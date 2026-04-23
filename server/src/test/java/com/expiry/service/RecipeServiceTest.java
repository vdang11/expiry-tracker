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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RecipeServiceTest {

    @Mock
    private RecipeAggregationService aggregationService;
    @Mock
    private RecipePromptBuilder promptBuilder;
    @Mock
    private OpenAIClient openAIClient;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private RecipeIngredientRepository recipeIngredientRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private IngredientService ingredientService;

    @InjectMocks
    private RecipeService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // =========================
    // TEST 1: no ingredients
    // =========================
    @Test
    void should_return_empty_when_no_ingredients() {

        when(aggregationService.getAggregationResult(1L))
                .thenReturn(new RecipeAggregationResult(
                        1L,
                        List.of(),
                        List.of(),
                        List.of()
                ));

        List<RecipeResponse> result = service.generateRecipes(1L, null);

        assertThat(result).isEmpty();
    }

    // =========================
    // TEST 2: DB đủ → không gọi AI
    // =========================
    @Test
    void should_use_db_when_enough_recipes() {

        RecipeAggregationResult aggregation =
                new RecipeAggregationResult(
                        1L,
                        List.of("milk"),
                        List.of(),
                        List.of("milk")
                );

        when(aggregationService.getAggregationResult(1L))
                .thenReturn(aggregation);

        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setTitle("Milk Soup");
        recipe.setIngredients("milk");

        when(recipeRepository.findReusableRecipes(any(), any(), any()))
                .thenReturn(List.of(recipe));

        List<RecipeResponse> result = service.generateRecipes(1L, null);

        assertThat(result).isNotEmpty();

        verify(openAIClient, never()).generateText(any());
    }

    // =========================
    // TEST 3: DB thiếu → gọi AI
    // =========================
    @Test
    void should_call_ai_when_db_not_enough() throws Exception {

        RecipeAggregationResult aggregation =
                new RecipeAggregationResult(
                        1L,
                        List.of("milk"),
                        List.of(),
                        List.of("milk")
                );

        when(aggregationService.getAggregationResult(1L))
                .thenReturn(aggregation);

        when(recipeRepository.findReusableRecipes(any(), any(), any()))
                .thenReturn(List.of());

        // ===== MOCK AI =====
        when(promptBuilder.buildPrompt(any(), any()))
                .thenReturn("prompt");

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

        // ===== MOCK USER =====
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(user));

        // ===== MOCK SAVE RECIPE (CRITICAL FIX) =====
        when(recipeRepository.save(any()))
                .thenAnswer(invocation -> {
                    Recipe r = invocation.getArgument(0);
                    r.setId(1L);
                    return r;
                });

        // ===== MOCK INGREDIENT FLOW =====
        Ingredient ingredient = new Ingredient();
        ingredient.setId(1L);

        when(ingredientService.findOrCreate(any()))
                .thenReturn(ingredient);

        when(recipeIngredientRepository.existsByRecipe_IdAndIngredient_Id(any(), any()))
                .thenReturn(false);

        when(recipeIngredientRepository.save(any()))
                .thenReturn(new RecipeIngredient());

        // ===== EXECUTE =====
        List<RecipeResponse> result = service.generateRecipes(1L, null);

        assertThat(result).isNotEmpty();

        verify(openAIClient, times(1)).generateText(any());
    }
}