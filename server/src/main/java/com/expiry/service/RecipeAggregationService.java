package com.expiry.service;

import com.expiry.dto.RecipeAggregationResult;
import com.expiry.entity.Item;
import com.expiry.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RecipeAggregationService {

    private static final String ACTIVE = "ACTIVE";
    private static final int EXPIRING_DAYS_THRESHOLD = 3;
    private static final int MAX_STABLE_ITEMS = 3;

    private final ItemRepository itemRepository;
    private final RecipeIngredientFilter recipeIngredientFilter;

    public List<String> getIngredientsForRecipe(Long userId) {
        return getAggregationResult(userId).getIngredients();
    }

    public RecipeAggregationResult getAggregationResult(Long userId) {

        LocalDate today = LocalDate.now();

        List<Item> items = itemRepository.findByUser_IdAndItemStatus(userId, ACTIVE);

        // ===== EXPIRING ITEMS =====
        List<String> expiring = items.stream()
                .filter(item -> isExpiringSoon(today, item.getExpiryDate()))
                .map(Item::getProductName)
                .filter(this::isValidName)
                .filter(recipeIngredientFilter::isCookableIngredient)
                .map(this::normalize)
                .distinct()
                .toList();

        // ===== STABLE ITEMS =====
        List<String> stable = items.stream()
                .filter(item -> isStable(today, item.getExpiryDate()))
                .map(Item::getProductName)
                .filter(this::isValidName)
                .filter(recipeIngredientFilter::isCookableIngredient)
                .map(this::normalize)
                .distinct()
                .limit(MAX_STABLE_ITEMS)
                .toList();

        List<String> all = mergeIngredients(expiring, stable);

        return new RecipeAggregationResult(userId, expiring, stable, all);
    }

    private boolean isExpiringSoon(LocalDate today, LocalDate expiryDate) {
        long days = daysBetween(today, expiryDate);
        return days >= 0 && days <= EXPIRING_DAYS_THRESHOLD;
    }

    private boolean isStable(LocalDate today, LocalDate expiryDate) {
        return daysBetween(today, expiryDate) > EXPIRING_DAYS_THRESHOLD;
    }

    private long daysBetween(LocalDate today, LocalDate date) {
        return ChronoUnit.DAYS.between(today, date);
    }

    private boolean isValidName(String name) {
        return name != null && !name.trim().isBlank();
    }

    private String normalize(String text) {
        return text.trim().toLowerCase();
    }

    private List<String> mergeIngredients(List<String> expiring, List<String> stable) {

        List<String> result = new ArrayList<>(expiring);

        for (String ingredient : stable) {
            if (!result.contains(ingredient)) {
                result.add(ingredient);
            }
        }

        return result;
    }
}