package com.expiry.service;

import com.expiry.dto.RecipeAggregationResult;
import com.expiry.dto.RecipeResponse;
import com.expiry.entity.Recipe;
import com.expiry.entity.User;
import com.expiry.infrastructure.ai.OpenAIClient;
import com.expiry.repository.RecipeRepository;
import com.expiry.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RecipeServiceTest {

    @Mock
    private RecipeAggregationService aggregationService;

    @Mock
    private RecipePromptBuilder promptBuilder;

    @Mock
    private OpenAIClient openAIClient;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RecipeService recipeService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // =========================================
    // CASE 1: DB đủ → KHÔNG gọi AI
    // =========================================
    @Test
    void should_not_call_ai_when_db_covers_all_expiring() {

        Long userId = 1L;

        RecipeAggregationResult agg = new RecipeAggregationResult(
                userId,
                List.of("milk", "egg"),
                List.of(),
                List.of("milk", "egg")
        );

        when(aggregationService.getAggregationResult(userId))
                .thenReturn(agg);

        Recipe recipe = new Recipe();
        recipe.setId(10L);
        recipe.setTitle("Omelette");
        recipe.setIngredients("milk, egg");
        recipe.setSteps("cook");

        when(recipeRepository.findByUser_Id(userId))
                .thenReturn(List.of(recipe));

        List<RecipeResponse> result =
                recipeService.generateRecipes(userId, List.of());


        verify(openAIClient, never()).generateText(any());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExpiringIngredients())
                .containsExactlyInAnyOrder("milk", "egg");
    }

    // =========================================
    // CASE 2: DB thiếu → GỌI AI
    // =========================================
    @Test
    void should_call_ai_when_db_missing_expiring() {

        Long userId = 1L;

        RecipeAggregationResult agg = new RecipeAggregationResult(
                userId,
                List.of("milk", "egg"),
                List.of(),
                List.of("milk", "egg")
        );

        when(aggregationService.getAggregationResult(userId))
                .thenReturn(agg);

        // DB chỉ cover milk
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setTitle("Milk drink");
        recipe.setIngredients("milk");
        recipe.setSteps("drink");

        when(recipeRepository.findByUser_Id(userId))
                .thenReturn(List.of(recipe));

        when(promptBuilder.buildPrompt(any(), any()))
                .thenReturn("prompt");

        // AI trả recipe cover egg
        String aiJson = """
        [
          {
            "title": "Boiled Egg",
            "ingredients": ["egg"],
            "steps": ["boil egg"]
          }
        ]
        """;

        when(openAIClient.generateText(any()))
                .thenReturn(aiJson);

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        List<RecipeResponse> result =
                recipeService.generateRecipes(userId, List.of());

        // ✅ AI phải được gọi
        verify(openAIClient, times(1)).generateText(any());

        assertThat(result.size()).isGreaterThanOrEqualTo(1);
    }

    // =========================================
    // CASE 3: EXPIRING FILTER ĐÚNG
    // =========================================
    @Test
    void should_assign_correct_expiring_ingredients() {

        Long userId = 1L;

        RecipeAggregationResult agg = new RecipeAggregationResult(
                userId,
                List.of("milk"),
                List.of(),
                List.of("milk", "bread")
        );

        when(aggregationService.getAggregationResult(userId))
                .thenReturn(agg);

        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setTitle("Milk Bread");
        recipe.setIngredients("milk, bread");
        recipe.setSteps("cook");

        when(recipeRepository.findByUser_Id(userId))
                .thenReturn(List.of(recipe));

        List<RecipeResponse> result =
                recipeService.generateRecipes(userId, List.of());

        assertThat(result.get(0).getExpiringIngredients())
                .containsExactly("milk");
    }
}