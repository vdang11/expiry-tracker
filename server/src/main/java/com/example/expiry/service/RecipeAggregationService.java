package com.example.expiry.service;

import com.example.expiry.dto.RecipeAggregationResult;
import com.example.expiry.entity.Item;
import com.example.expiry.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
public class RecipeAggregationService {

    private final ProductRepository productRepository;
    private final RecipeIngredientFilter recipeIngredientFilter;
    private final RecipeCacheKeyBuilder recipeCacheKeyBuilder;

    public RecipeAggregationService(ProductRepository productRepository,
                                    RecipeIngredientFilter recipeIngredientFilter,
                                    RecipeCacheKeyBuilder recipeCacheKeyBuilder) {
        this.productRepository = productRepository;
        this.recipeIngredientFilter = recipeIngredientFilter;
        this.recipeCacheKeyBuilder = recipeCacheKeyBuilder;
    }

    public List<String> getIngredientsForRecipe(Long userId) {
        return getAggregationResult(userId).getIngredients();
    }

    public RecipeAggregationResult getAggregationResult(Long userId) {
        LocalDate today = LocalDate.now();

        List<String> ingredients = productRepository
                .findByUser_IdAndItemStatus(userId, "ACTIVE")
                .stream()
                .filter(item -> item.getExpiryDate() != null)
                .filter(item -> {
                    long days = ChronoUnit.DAYS.between(today, item.getExpiryDate());
                    return days >= 0 && days <= 3;
                })
                .map(Item::getProductName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .filter(recipeIngredientFilter::isCookableIngredient)
                .distinct()
                .toList();

        String cacheKey = recipeCacheKeyBuilder.build(ingredients);

        System.out.println("[RecipeAggregationService] FILTERED INGREDIENTS: " + ingredients);
        System.out.println("[RecipeAggregationService] CACHE KEY: " + cacheKey);

        return new RecipeAggregationResult(userId, ingredients, cacheKey);
    }
}