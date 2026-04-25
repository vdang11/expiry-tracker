package com.expiry.service;

import com.expiry.dto.RecipeAggregationResult;
import com.expiry.dto.RecipeResponse;
import com.expiry.entity.Ingredient;
import com.expiry.entity.Recipe;
import com.expiry.entity.RecipeIngredient;
import com.expiry.entity.User;
import com.expiry.infrastructure.ai.OpenAIClient;
import com.expiry.repository.RecipeIngredientRepository;
import com.expiry.repository.RecipeRepository;
import com.expiry.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeAggregationService recipeAggregationService;
    private final RecipePromptBuilder recipePromptBuilder;
    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;
    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final UserRepository userRepository;
    private final IngredientService ingredientService;

    private static final int TARGET_RECIPES = 3;
    private static final int MAX_RECIPES = 6;

    public List<RecipeResponse> generateRecipes(Long userId, List<Long> excludeRecipeIds) {

        if (excludeRecipeIds == null) {
            excludeRecipeIds = List.of();
        }

        RecipeAggregationResult aggregation =
                recipeAggregationService.getAggregationResult(userId);

        List<String> allIngredients = aggregation.getIngredients();
        List<String> expiring = aggregation.getExpiringIngredients()
                .stream()
                .map(this::normalize)
                .toList();

        if (allIngredients == null || allIngredients.isEmpty()) {
            return Collections.emptyList();
        }

        List<RecipeResponse> result = new ArrayList<>();
        Set<String> remaining = new HashSet<>(expiring);

        if (expiring.isEmpty()) {
            return generateAndSave(userId, aggregation);
        }

        List<Recipe> candidates = recipeRepository.findReusableRecipes(
                userId,
                allIngredients,
                excludeRecipeIds.isEmpty() ? null : excludeRecipeIds
        );

        List<Recipe> ranked = rankReusableRecipes(candidates, expiring, allIngredients);

        if (ranked.isEmpty()) {
            return generateAndSave(userId, aggregation);
        }

        for (Recipe recipe : ranked) {

            if (remaining.isEmpty()) break;

            List<String> recipeIngredients = splitByComma(recipe.getIngredients())
                    .stream()
                    .map(this::normalize)
                    .toList();

            Set<String> matched = recipeIngredients.stream()
                    .filter(remaining::contains)
                    .collect(Collectors.toSet());

            if (matched.isEmpty()) continue;

            result.add(mapToResponse(recipe, false, expiring));
            remaining.removeAll(matched);
        }

        boolean needAI =
                result.size() < TARGET_RECIPES
                        || remaining.size() >= 2;

        if (needAI) {

            List<RecipeResponse> generated = generateAndSave(userId, aggregation);

            List<RecipeResponse> rankedAI = generated.stream()
                    .sorted((a, b) ->
                            Integer.compare(
                                    countMatchedExpiring(b, expiring),
                                    countMatchedExpiring(a, expiring)
                            )
                    )
                    .toList();

            for (RecipeResponse recipe : rankedAI) {

                if (result.size() >= MAX_RECIPES) break;

                List<String> ingredients = recipe.getIngredients()
                        .stream()
                        .map(this::normalize)
                        .toList();

                Set<String> matched = ingredients.stream()
                        .filter(remaining::contains)
                        .collect(Collectors.toSet());

                boolean duplicate = result.stream()
                        .anyMatch(existing ->
                                isSameRecipe(existing.getTitle(), recipe.getTitle())
                        );

                if (!duplicate) {
                    result.add(recipe);
                    remaining.removeAll(matched);
                }

                if (remaining.isEmpty() && result.size() >= TARGET_RECIPES) break;
            }
        }

        return result;
    }

    private List<RecipeResponse> generateAndSave(
            Long userId,
            RecipeAggregationResult aggregation
    ) {

        List<String> normalizedExpiring = aggregation.getExpiringIngredients()
                .stream()
                .map(this::normalize)
                .toList();

        String prompt = recipePromptBuilder.buildPrompt(
                aggregation.getIngredients(),
                aggregation.getExpiringIngredients()
        );

        String rawText = openAIClient.generateText(prompt);

        List<RecipeResponse> responses = parseResponse(rawText);

        List<RecipeResponse> valid = responses.stream()
                .filter(recipe -> hasExpiringIngredient(recipe, normalizedExpiring))
                .toList();

        if (valid.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "AI returned no valid recipes"
            );
        }

        List<RecipeResponse> saved = new ArrayList<>();

        for (RecipeResponse recipeResponse : valid) {
            Recipe savedRecipe = saveRecipeWithIngredients(userId, recipeResponse);
            saved.add(mapToResponse(savedRecipe, true, normalizedExpiring));
        }

        return saved;
    }

    private Recipe saveRecipeWithIngredients(
            Long userId,
            RecipeResponse response
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        Recipe recipe = new Recipe();
        recipe.setUser(user);
        recipe.setTitle(safeText(response.getTitle()));
        recipe.setIngredients(joinSafe(response.getIngredients(), ", "));
        recipe.setSteps(joinSafe(response.getSteps(), "\n"));

        Recipe saved = recipeRepository.save(recipe);

        List<String> normalizedIngredients = normalizeAndFilterIngredients(
                response.getIngredients()
        );

        for (String name : normalizedIngredients) {
            Ingredient ingredient = ingredientService.findOrCreate(name);

            boolean exists = recipeIngredientRepository
                    .existsByRecipe_IdAndIngredient_Id(
                            saved.getId(),
                            ingredient.getId()
                    );

            if (!exists) {
                RecipeIngredient mapping = new RecipeIngredient();
                mapping.setRecipe(saved);
                mapping.setIngredient(ingredient);
                recipeIngredientRepository.save(mapping);
            }
        }

        return saved;
    }

    private List<RecipeResponse> parseResponse(String rawText) {
        try {
            String cleaned = rawText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            return objectMapper.readValue(
                    cleaned,
                    new TypeReference<List<RecipeResponse>>() {}
            );

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid AI response format"
            );
        }
    }

    private RecipeResponse mapToResponse(
            Recipe recipe,
            boolean fromAI,
            List<String> globalExpiring
    ) {

        List<String> recipeIngredients = splitByComma(recipe.getIngredients())
                .stream()
                .map(this::normalize)
                .toList();

        List<String> matchedExpiring = globalExpiring.stream()
                .filter(recipeIngredients::contains)
                .toList();

        return RecipeResponse.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .ingredients(splitByComma(recipe.getIngredients()))
                .steps(splitByNewline(recipe.getSteps()))
                .fromAI(fromAI)
                .expiringIngredients(matchedExpiring)
                .build();
    }

    private List<Recipe> rankReusableRecipes(
            List<Recipe> candidates,
            List<String> expiring,
            List<String> all
    ) {

        return candidates.stream()
                .sorted((a, b) -> Integer.compare(
                        calculateScore(b, expiring, all),
                        calculateScore(a, expiring, all)
                ))
                .toList();
    }

    private int calculateScore(
            Recipe recipe,
            List<String> expiring,
            List<String> all
    ) {

        List<String> ingredients = splitByComma(recipe.getIngredients());

        int score = 0;

        for (String ingredient : ingredients) {
            String normalized = normalize(ingredient);

            if (expiring.contains(normalized)) {
                score += 5;
            } else if (all.contains(normalized)) {
                score += 2;
            }
        }

        return score;
    }

    private int countMatchedExpiring(
            RecipeResponse recipe,
            List<String> expiring
    ) {

        Set<String> expiringSet = new HashSet<>(expiring);

        return (int) recipe.getIngredients()
                .stream()
                .map(this::normalize)
                .filter(expiringSet::contains)
                .count();
    }

    private boolean hasExpiringIngredient(
            RecipeResponse recipe,
            List<String> expiring
    ) {

        if (expiring == null || expiring.isEmpty()) {
            return true;
        }

        return countMatchedExpiring(recipe, expiring) > 0;
    }

    private List<String> splitByComma(String text) {
        if (text == null || text.isBlank()) return List.of();

        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private List<String> splitByNewline(String text) {
        if (text == null || text.isBlank()) return List.of();

        return Arrays.stream(text.split("\\n"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private List<String> normalizeAndFilterIngredients(List<String> raw) {
        if (raw == null || raw.isEmpty()) return List.of();

        return raw.stream()
                .map(this::normalize)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    private String joinSafe(List<String> list, String delimiter) {
        if (list == null || list.isEmpty()) return "";
        return String.join(delimiter, list);
    }

    private String safeText(String text) {
        return text == null ? "" : text.trim();
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase();
    }

    private boolean isSameRecipe(String a, String b) {
        return normalize(a).equals(normalize(b));
    }
}