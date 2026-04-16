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
import org.springframework.transaction.annotation.Transactional;
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

    // ===== ENTRY =====
    public List<RecipeResponse> generateRecipes(Long userId, List<Long> excludeRecipeIds) {

        if (excludeRecipeIds == null) {
            excludeRecipeIds = List.of();
        }

        RecipeAggregationResult aggregation =
                recipeAggregationService.getAggregationResult(userId);

        List<String> ingredients = aggregation.getIngredients();
        List<String> expiringIngredients = aggregation.getExpiringIngredients()
                .stream()
                .map(this::normalize)
                .toList();

        if (ingredients == null || ingredients.isEmpty()) {
            return Collections.emptyList();
        }

        List<RecipeResponse> result = new ArrayList<>();
        Set<String> uncovered = new HashSet<>(expiringIngredients);

        // ===== DB REUSE =====
        List<Recipe> candidates = recipeRepository.findReusableRecipes(
                userId,
                ingredients,
                excludeRecipeIds.isEmpty() ? null : excludeRecipeIds
        );

        List<Recipe> ranked = rankReusableRecipes(
                candidates,
                expiringIngredients,
                ingredients
        );

        for (Recipe recipe : ranked) {

            if (uncovered.isEmpty()) break;

            List<String> recipeIngredients = splitByComma(recipe.getIngredients())
                    .stream()
                    .map(this::normalize)
                    .toList();

            boolean contributes = recipeIngredients.stream()
                    .anyMatch(uncovered::contains);

            if (!contributes) continue;

            result.add(mapToResponse(recipe, false, expiringIngredients));

            uncovered.removeAll(recipeIngredients);
        }

        // ===== AI FALLBACK =====
        if (!uncovered.isEmpty()) {

            List<RecipeResponse> generated =
                    generateAndSave(userId, aggregation);

            for (RecipeResponse recipe : generated) {

                List<String> recipeIngredients = recipe.getIngredients().stream()
                        .map(this::normalize)
                        .toList();

                boolean contributes = recipeIngredients.stream()
                        .anyMatch(uncovered::contains);

                if (!contributes) continue;

                boolean duplicate = result.stream()
                        .anyMatch(r -> isSameRecipe(r.getTitle(), recipe.getTitle()));

                if (!duplicate) {
                    result.add(recipe);
                    uncovered.removeAll(recipeIngredients);
                }

                if (uncovered.isEmpty()) break;
            }
        }

        return result;
    }

    // ===== AI GENERATION =====
    @Transactional
    private List<RecipeResponse> generateAndSave(Long userId,
                                                 RecipeAggregationResult aggregation) {

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

        if (responses.isEmpty()) {
            return List.of();
        }

        List<RecipeResponse> saved = new ArrayList<>();

        for (RecipeResponse r : responses) {
            Recipe savedRecipe = saveRecipeWithIngredients(userId, r);

            saved.add(mapToResponse(savedRecipe, true, normalizedExpiring));
        }

        return saved;
    }

    // =====FIX QUAN TRỌNG NHẤT =====
    private RecipeResponse mapToResponse(
            Recipe recipe,
            boolean fromAI,
            List<String> globalExpiring
    ) {

        List<String> recipeIngredients = splitByComma(recipe.getIngredients())
                .stream()
                .map(this::normalize)
                .toList();

        //CHỈ LẤY EXPIRING CÓ TRONG RECIPE
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

    // ===== PARSE =====
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

    // ===== SAVE =====
    private Recipe saveRecipeWithIngredients(Long userId,
                                             RecipeResponse response) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
                );

        Recipe recipe = Recipe.builder()
                .user(user)
                .title(safeText(response.getTitle()))
                .ingredients(joinSafe(response.getIngredients(), ", "))
                .steps(joinSafe(response.getSteps(), "\n"))
                .build();

        Recipe saved = recipeRepository.save(recipe);

        List<String> raw = response.getIngredients();
        List<String> normalized = normalizeAndFilterIngredients(raw);

        for (String name : normalized) {
            Ingredient ingredient = ingredientService.findOrCreate(name);

            boolean exists = recipeIngredientRepository
                    .existsByRecipe_IdAndIngredient_Id(
                            saved.getId(),
                            ingredient.getId()
                    );

            if (!exists) {
                try {
                    RecipeIngredient mapping = RecipeIngredient.builder()
                            .recipe(saved)
                            .ingredient(ingredient)
                            .build();

                    recipeIngredientRepository.save(mapping);
                } catch (Exception ignored) {}
            }
        }

        return saved;
    }

    // ===== UTILS =====
    private List<String> splitByComma(String text) {
        if (text == null || text.isBlank()) return List.of();

        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private List<String> splitByNewline(String text) {
        if (text == null || text.isBlank()) return List.of();

        return Arrays.stream(text.split("\\n"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
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

    private List<String> normalizeAndFilterIngredients(List<String> raw) {
        if (raw == null || raw.isEmpty()) return List.of();

        Set<String> set = new LinkedHashSet<>();

        for (String r : raw) {
            String n = normalize(r);
            if (!n.isBlank()) set.add(n);
        }

        return new ArrayList<>(set);
    }

    private List<Recipe> rankReusableRecipes(List<Recipe> candidates,
                                             List<String> expiring,
                                             List<String> all) {

        return candidates.stream()
                .sorted((a, b) -> Integer.compare(
                        calculateScore(b, expiring, all),
                        calculateScore(a, expiring, all)
                ))
                .toList();
    }

    private int calculateScore(Recipe recipe,
                               List<String> expiring,
                               List<String> all) {

        List<String> ing = splitByComma(recipe.getIngredients());

        int score = 0;

        for (String i : ing) {
            String n = normalize(i);

            if (expiring.contains(n)) score += 5;
            else if (all.contains(n)) score += 2;
        }

        return score;
    }
}