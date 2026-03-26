package com.example.expiry.service;

import com.example.expiry.dto.RecipeAggregationResult;
import com.example.expiry.dto.RecipeResponse;
import com.example.expiry.entity.Recipe;
import com.example.expiry.infrastructure.ai.OpenAIClient;
import com.example.expiry.repository.RecipeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class RecipeService {

    private final RecipeAggregationService recipeAggregationService;
    private final RecipePromptBuilder recipePromptBuilder;
    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;
    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeAggregationService recipeAggregationService,
                         RecipePromptBuilder recipePromptBuilder,
                         OpenAIClient openAIClient,
                         ObjectMapper objectMapper,
                         RecipeRepository recipeRepository) {
        this.recipeAggregationService = recipeAggregationService;
        this.recipePromptBuilder = recipePromptBuilder;
        this.openAIClient = openAIClient;
        this.objectMapper = objectMapper;
        this.recipeRepository = recipeRepository;
    }

    public List<RecipeResponse> generateRecipes(Long userId) {
        RecipeAggregationResult aggregation =
                recipeAggregationService.getAggregationResult(userId);

        List<String> ingredients = aggregation.getIngredients();

        if (ingredients == null || ingredients.isEmpty()) {
            return Collections.emptyList();
        }

        String cacheKey = aggregation.getCacheKey();

        List<Recipe> cached = recipeRepository.findAllByCacheKey(cacheKey);

        if (!cached.isEmpty()) {
            System.out.println("[RecipeService] CACHE HIT");

            return cached.stream()
                    .sorted(Comparator.comparing(Recipe::getId))
                    .map(recipe -> mapToResponse(recipe, true))
                    .toList();
        }

        return generateAndCache(userId, aggregation, cacheKey);
    }

    private List<RecipeResponse> generateAndCache(Long userId,
                                                  RecipeAggregationResult aggregation,
                                                  String cacheKey) {

        System.out.println("[RecipeService] CACHE MISS -> CALL AI");

        String prompt = recipePromptBuilder.buildPrompt(aggregation.getIngredients());

        String rawText = openAIClient.generateText(prompt);

        List<RecipeResponse> responses = parseResponse(rawText);

        if (responses.isEmpty()) {
            return responses;
        }

        responses.forEach(response -> saveCache(userId, cacheKey, response));

        return responses;
    }

    private List<RecipeResponse> parseResponse(String rawText) {
        try {
            String cleaned = rawText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            System.out.println("[RecipeService] RAW AI RESPONSE:\n" + cleaned);

            return objectMapper.readValue(
                    cleaned,
                    new TypeReference<List<RecipeResponse>>() {}
            );
        } catch (Exception e) {
            System.out.println("[RecipeService] PARSE ERROR RAW:\n" + rawText);
            throw new RuntimeException("Invalid AI response format", e);
        }
    }

    private void saveCache(Long userId,
                           String cacheKey,
                           RecipeResponse response) {

        Recipe recipe = Recipe.builder()
                .userId(userId)
                .title(response.getTitle())
                .ingredients(joinSafe(response.getIngredients(), ", "))
                .steps(joinSafe(response.getSteps(), "\n"))
                .cacheKey(cacheKey)
                .build();

        try {
            recipeRepository.save(recipe);
        } catch (Exception e) {
            System.out.println("[RecipeService] Cache save conflict");
        }
    }

    private RecipeResponse mapToResponse(Recipe recipe, boolean fromCache) {
        return RecipeResponse.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .ingredients(splitSafe(recipe.getIngredients(), ","))
                .steps(splitSafe(recipe.getSteps(), "\n"))
                .fromCache(fromCache)
                .build();
    }

    private String joinSafe(List<String> list, String delimiter) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return String.join(delimiter, list);
    }

    private List<String> splitSafe(String text, String delimiter) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return List.of(text.split(delimiter))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}