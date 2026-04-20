package com.expiry.service;

import com.expiry.dto.RecipeAggregationResult;
import com.expiry.entity.Item;
import com.expiry.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RecipeAggregationService {

    private final ProductRepository productRepository;
    private final RecipeIngredientFilter recipeIngredientFilter;

    public List<String> getIngredientsForRecipe(Long userId) {
        return getAggregationResult(userId).getIngredients();
    }

    public RecipeAggregationResult getAggregationResult(Long userId) {

        LocalDate today = LocalDate.now();

        List<Item> items = productRepository.findByUser_IdAndItemStatus(userId, "ACTIVE");

        List<String> expiring = items.stream()
                .filter(item -> item.getExpiryDate() != null)
                .filter(item -> daysBetween(today, item.getExpiryDate()) <= 3)
                .map(Item::getProductName)
                .filter(this::isValidName)
                .filter(recipeIngredientFilter::isCookableIngredient)
                .map(this::normalize)
                .distinct()
                .toList();

        List<String> stable = items.stream()
                .filter(item -> item.getExpiryDate() != null)
                .filter(item -> daysBetween(today, item.getExpiryDate()) > 3)
                .map(Item::getProductName)
                .filter(this::isValidName)
                .filter(recipeIngredientFilter::isCookableIngredient)
                .map(this::normalize)
                .distinct()
                .limit(3)
                .toList();

        List<String> all = new ArrayList<>(expiring);

        for (String s : stable) {
            if (!all.contains(s)) {
                all.add(s);
            }
        }

        return new RecipeAggregationResult(userId, expiring, stable, all);
    }

    // ===== helpers =====

    private long daysBetween(LocalDate today, LocalDate date) {
        return ChronoUnit.DAYS.between(today, date);
    }

    private boolean isValidName(String name) {
        return name != null && !name.trim().isBlank();
    }

    private String normalize(String text) {
        return text.trim().toLowerCase();
    }
}