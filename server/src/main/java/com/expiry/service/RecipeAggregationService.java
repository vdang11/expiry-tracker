package com.expiry.service;

import com.expiry.dto.RecipeAggregationResult;
import com.expiry.entity.Item;
import com.expiry.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class RecipeAggregationService {

    private final ProductRepository productRepository;
    private final RecipeIngredientFilter recipeIngredientFilter;

    public RecipeAggregationService(ProductRepository productRepository,
                                    RecipeIngredientFilter recipeIngredientFilter) {
        this.productRepository = productRepository;
        this.recipeIngredientFilter = recipeIngredientFilter;
    }

    public List<String> getIngredientsForRecipe(Long userId) {
        return getAggregationResult(userId).getIngredients();
    }

    public RecipeAggregationResult getAggregationResult(Long userId) {

        LocalDate today = LocalDate.now();

        List<Item> items = productRepository.findByUser_IdAndItemStatus(userId, "ACTIVE");

        // 🔥 FIX: include expired items
        List<String> expiring = items.stream()
                .filter(item -> item.getExpiryDate() != null)
                .filter(item -> {
                    long days = ChronoUnit.DAYS.between(today, item.getExpiryDate());
                    return days <= 3; // ✅ CHANGED (was >=0 && <=3)
                })
                .map(Item::getProductName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .filter(recipeIngredientFilter::isCookableIngredient)
                .map(this::normalize) // 🔥 normalize luôn từ đầu
                .distinct()
                .toList();

        List<String> stable = items.stream()
                .filter(item -> item.getExpiryDate() != null)
                .filter(item -> {
                    long days = ChronoUnit.DAYS.between(today, item.getExpiryDate());
                    return days > 3;
                })
                .map(Item::getProductName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .filter(recipeIngredientFilter::isCookableIngredient)
                .map(this::normalize)
                .distinct()
                .limit(3)
                .toList();

        List<String> allIngredients = new ArrayList<>();
        allIngredients.addAll(expiring);

        for (String ingredient : stable) {
            if (!allIngredients.contains(ingredient)) {
                allIngredients.add(ingredient);
            }
        }

        return new RecipeAggregationResult(userId, expiring, stable, allIngredients);
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase();
    }
}