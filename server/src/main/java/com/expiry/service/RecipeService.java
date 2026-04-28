package com.expiry.service;

import com.expiry.dto.*;
import com.expiry.entity.*;
import com.expiry.infrastructure.ai.OpenAIClient;
import com.expiry.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {

    private final RecipeAggregationService aggregationService;
    private final RecipePromptBuilder promptBuilder;
    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    private static final int MAX_AI_CALLS = 3;
    private static final int MAX_RECIPES = 15;

    public List<RecipeResponse> generateRecipes(Long userId, List<Long> excludeIds) {

        List<Long> excludedRecipeIds =
                excludeIds == null ? List.of() : excludeIds;

        log.info("=== GENERATE START === userId={}, excludeIds={}", userId, excludedRecipeIds);

        RecipeAggregationResult agg =
                aggregationService.getAggregationResult(userId);

        List<String> allIngredients = normalizeList(agg.getIngredients());
        List<String> expiring = normalizeList(agg.getExpiringIngredients());

        if (expiring.isEmpty()) return List.of();

        Set<String> uncovered = new HashSet<>(expiring);
        List<RecipeResponse> result = new ArrayList<>();

        log.info("Expiring total = {}", uncovered.size());

        // ================= DB FIRST =================
        List<Recipe> dbRecipes = recipeRepository.findByUser_Id(userId)
                .stream()
                .filter(r -> !excludedRecipeIds.contains(r.getId())) // 🔥 FIX
                .toList();

        for (Recipe r : dbRecipes) {

            List<String> ingredients = split(r.getIngredients());

            Set<String> covered =
                    findCoveredExpiring(ingredients, uncovered);

            if (!covered.isEmpty()) {

                result.add(map(r, false, new ArrayList<>(covered)));

                uncovered.removeAll(covered);

                log.info("DB recipe used: {} -> cover {}", r.getTitle(), covered);
            }

            if (uncovered.isEmpty()) break;
        }

        log.info("After DB, uncovered = {}", uncovered);

        // ================= AI FALLBACK =================
        int aiCall = 0;

        while (!uncovered.isEmpty()
                && aiCall < MAX_AI_CALLS
                && result.size() < MAX_RECIPES) {

            aiCall++;

            log.info("AI call {} - remaining {}", aiCall, uncovered);

            String prompt =
                    promptBuilder.buildPrompt(allIngredients, new ArrayList<>(uncovered));

            String raw = openAIClient.generateText(prompt);

            List<RecipeResponse> parsed = parse(raw);

            for (RecipeResponse r : parsed) {

                Set<String> covered =
                        findCoveredExpiring(r.getIngredients(), uncovered);

                if (covered.isEmpty()) continue;

                Recipe saved = save(userId, r);

                // không add lại recipe đã exclude
                if (saved != null && !excludedRecipeIds.contains(saved.getId())) {

                    result.add(
                            RecipeResponse.builder()
                                    .id(saved.getId())
                                    .title(saved.getTitle())
                                    .ingredients(r.getIngredients())
                                    .steps(r.getSteps())
                                    .fromAI(true)
                                    .expiringIngredients(new ArrayList<>(covered))
                                    .build()
                    );

                    uncovered.removeAll(covered);

                    log.info("AI recipe used: {} -> cover {}", r.getTitle(), covered);
                }

                if (uncovered.isEmpty()) break;
            }
        }

        // ================= FINAL =================
        if (!uncovered.isEmpty()) {
            log.warn("Still not covered: {}", uncovered);
        } else {
            log.info("All expiring items covered");
        }

        log.info("=== DONE total_recipes={} ===", result.size());

        return result;
    }

    // ================= HELPERS =================

    private Set<String> findCoveredExpiring(List<String> ingredients, Set<String> expiring) {

        Set<String> covered = new HashSet<>();

        for (String e : expiring) {
            for (String i : ingredients) {
                if (isMatch(i, e)) {
                    covered.add(e);
                    break;
                }
            }
        }

        return covered;
    }

    private boolean isMatch(String a, String b) {

        String x = normalize(a);
        String y = normalize(b);

        if (x.equals(y)) return true;
        if (x.contains(y) || y.contains(x)) return true;

        Set<String> s1 = new HashSet<>(Arrays.asList(x.split(" ")));
        Set<String> s2 = new HashSet<>(Arrays.asList(y.split(" ")));

        s1.retainAll(s2);

        return !s1.isEmpty();
    }

    private Recipe save(Long userId, RecipeResponse r) {

        User user = userRepository.findById(userId).orElseThrow();

        Recipe recipe = new Recipe();
        recipe.setUser(user);
        recipe.setTitle(r.getTitle());
        recipe.setIngredients(String.join(", ", r.getIngredients()));
        recipe.setSteps(String.join("\n", r.getSteps()));

        try {
            return recipeRepository.save(recipe);
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate recipe skipped: {}", r.getTitle());
            return null;
        }
    }

    private RecipeResponse map(Recipe r, boolean fromAI, List<String> expiring) {

        return RecipeResponse.builder()
                .id(r.getId())
                .title(r.getTitle())
                .ingredients(split(r.getIngredients()))
                .steps(splitLines(r.getSteps()))
                .fromAI(fromAI)
                .expiringIngredients(expiring)
                .build();
    }

    private List<String> split(String text) {
        if (text == null) return List.of();

        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private List<String> splitLines(String text) {
        if (text == null) return List.of();

        return Arrays.stream(text.split("\\n"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private List<RecipeResponse> parse(String raw) {
        try {
            String cleaned = raw.replace("```json", "").replace("```", "").trim();

            return objectMapper.readValue(
                    cleaned,
                    new TypeReference<List<RecipeResponse>>() {}
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "AI parse error");
        }
    }

    private List<String> normalizeList(List<String> list) {
        if (list == null) return List.of();

        return list.stream()
                .map(this::normalize)
                .toList();
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }
}