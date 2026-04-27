package com.expiry.service;

import com.expiry.dto.RecipeAggregationResult;
import com.expiry.dto.RecipeResponse;
import com.expiry.entity.Ingredient;
import com.expiry.entity.Recipe;
import com.expiry.entity.User;
import com.expiry.infrastructure.ai.OpenAIClient;
import com.expiry.repository.RecipeIngredientRepository;
import com.expiry.repository.RecipeRepository;
import com.expiry.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RecipeServiceTest {

    private final RecipeAggregationService recipeAggregationService = mock(RecipeAggregationService.class);
    private final RecipePromptBuilder recipePromptBuilder = mock(RecipePromptBuilder.class);
    private final OpenAIClient openAIClient = mock(OpenAIClient.class);
    private final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private final RecipeRepository recipeRepository = mock(RecipeRepository.class);
    private final RecipeIngredientRepository recipeIngredientRepository = mock(RecipeIngredientRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final IngredientService ingredientService = mock(IngredientService.class);

    private final RecipeService service = new RecipeService(
            recipeAggregationService,
            recipePromptBuilder,
            openAIClient,
            objectMapper,
            recipeRepository,
            recipeIngredientRepository,
            userRepository,
            ingredientService
    );

    // =========================================
    // TEST 1 — CALL AI WHEN DB NOT ENOUGH
    // =========================================
    @Test
    void should_call_ai_when_db_not_enough() throws Exception {

        RecipeAggregationResult aggregation =
                new RecipeAggregationResult(
                        1L,
                        List.of("milk"),
                        List.of(),
                        List.of("milk")
                );

        when(recipeAggregationService.getAggregationResult(1L))
                .thenReturn(aggregation);

        when(recipeRepository.findReusableRecipes(any(), any(), any()))
                .thenReturn(List.of());

        when(recipePromptBuilder.buildPrompt(any(), any()))
                .thenReturn("prompt");

        User user = new User();
        user.setId(1L);

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(user));

        when(openAIClient.generateText(any()))
                .thenReturn("""
                [
                  {
                    "title": "AI Recipe",
                    "ingredients": ["milk"],
                    "steps": ["step1"]
                  }
                ]
                """);

        when(objectMapper.readValue(
                anyString(),
                ArgumentMatchers.<TypeReference<List<RecipeResponse>>>any()
        )).thenReturn(List.of(
                RecipeResponse.builder()
                        .title("AI Recipe")
                        .ingredients(List.of("milk"))
                        .steps(List.of("step1"))
                        .build()
        ));

        when(recipeRepository.save(any()))
                .thenAnswer(invocation -> {
                    Recipe r = invocation.getArgument(0);
                    r.setId(1L);
                    return r;
                });

        when(ingredientService.findOrCreate(any()))
                .thenAnswer(invocation -> {
                    Ingredient i = new Ingredient();
                    i.setId(1L);
                    return i;
                });

        when(recipeIngredientRepository.existsByRecipe_IdAndIngredient_Id(any(), any()))
                .thenReturn(false);

        List<RecipeResponse> result = service.generateRecipes(1L, null);

        assertThat(result).isNotEmpty();

        verify(openAIClient, times(1)).generateText(any());
    }

    // =========================================
    // TEST 2 — USE DB (BUT STILL FALLBACK AI)
    // =========================================
    @Test
    void should_use_db_when_enough_recipes() throws Exception {

        RecipeAggregationResult aggregation =
                new RecipeAggregationResult(
                        1L,
                        List.of("milk"),
                        List.of(),
                        List.of("milk")
                );

        when(recipeAggregationService.getAggregationResult(1L))
                .thenReturn(aggregation);

        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setTitle("Milk Soup");
        recipe.setIngredients("milk");

        when(recipeRepository.findReusableRecipes(any(), any(), any()))
                .thenReturn(List.of(recipe));

        User user = new User();
        user.setId(1L);

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(user));

        when(recipeRepository.save(any()))
                .thenAnswer(invocation -> {
                    Recipe r = invocation.getArgument(0);
                    r.setId(1L);
                    return r;
                });

        when(openAIClient.generateText(any()))
                .thenReturn("""
                [
                  {
                    "title": "Milk Soup",
                    "ingredients": ["milk"],
                    "steps": ["step1"]
                  }
                ]
                """);

        when(objectMapper.readValue(
                anyString(),
                ArgumentMatchers.<TypeReference<List<RecipeResponse>>>any()
        )).thenReturn(List.of(
                RecipeResponse.builder()
                        .title("Milk Soup")
                        .ingredients(List.of("milk"))
                        .steps(List.of("step1"))
                        .build()
        ));

        when(ingredientService.findOrCreate(any()))
                .thenAnswer(invocation -> {
                    Ingredient i = new Ingredient();
                    i.setId(1L);
                    return i;
                });

        when(recipeIngredientRepository.existsByRecipe_IdAndIngredient_Id(any(), any()))
                .thenReturn(false);

        List<RecipeResponse> result = service.generateRecipes(1L, null);

        assertThat(result).isNotEmpty();

        //thực tế vẫn gọi AI
        verify(openAIClient, times(1)).generateText(any());
    }

    // =========================================
    // TEST 3 — EXCLUDE RECIPES
    // =========================================
    @Test
    void should_exclude_recipes() throws Exception {

        RecipeAggregationResult aggregation =
                new RecipeAggregationResult(
                        1L,
                        List.of("milk"),
                        List.of(),
                        List.of("milk")
                );

        when(recipeAggregationService.getAggregationResult(1L))
                .thenReturn(aggregation);

        Recipe recipe = new Recipe();
        recipe.setId(2L);
        recipe.setTitle("Milk Soup");
        recipe.setIngredients("milk");

        when(recipeRepository.findReusableRecipes(any(), any(), any()))
                .thenReturn(List.of(recipe));

        User user = new User();
        user.setId(1L);

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(user));

        when(recipeRepository.save(any()))
                .thenAnswer(invocation -> {
                    Recipe r = invocation.getArgument(0);
                    r.setId(2L);
                    return r;
                });

        when(openAIClient.generateText(any()))
                .thenReturn("""
                [
                  {
                    "title": "Another Recipe",
                    "ingredients": ["milk"],
                    "steps": ["step1"]
                  }
                ]
                """);

        when(objectMapper.readValue(
                anyString(),
                ArgumentMatchers.<TypeReference<List<RecipeResponse>>>any()
        )).thenReturn(List.of(
                RecipeResponse.builder()
                        .title("Another Recipe")
                        .ingredients(List.of("milk"))
                        .steps(List.of("step1"))
                        .build()
        ));

        when(ingredientService.findOrCreate(any()))
                .thenAnswer(invocation -> {
                    Ingredient i = new Ingredient();
                    i.setId(1L);
                    return i;
                });

        when(recipeIngredientRepository.existsByRecipe_IdAndIngredient_Id(any(), any()))
                .thenReturn(false);

        List<RecipeResponse> result = service.generateRecipes(1L, List.of(2L));

        assertThat(result).isNotEmpty();
    }
}