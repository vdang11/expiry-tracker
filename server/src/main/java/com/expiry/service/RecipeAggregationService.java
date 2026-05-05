package com.expiry.service;

import com.expiry.dto.RecipeAggregationResult;
import com.expiry.entity.Item;
import com.expiry.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
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

        log.info("===== AGG START userId={} =====", userId);
        log.info("TOTAL ACTIVE items = {}", items.size());

        // ================= EXPIRED (DEBUG ONLY) =================
        List<Item> expired = items.stream()
                .filter(item -> isExpired(today, item.getExpiryDate()))
                .toList();

        logItems("EXCLUDED EXPIRED items", expired);

        // ================= EXPIRING RAW =================
        List<Item> expiringRaw = items.stream()
                .filter(item -> isExpiringSoon(today, item.getExpiryDate()))
                .toList();

        log.info("EXPIRING RAW count = {}", expiringRaw.size());

        // ================= INVALID NAME =================
        List<Item> invalidName = expiringRaw.stream()
                .filter(item -> !isValidName(item.getProductName()))
                .toList();

        logItems("INVALID NAME items", invalidName);

        // ================= NOT COOKABLE =================
        List<Item> notCookable = expiringRaw.stream()
                .filter(item -> isValidName(item.getProductName()))
                .filter(item -> !recipeIngredientFilter.isCookableIngredient(item.getProductName()))
                .toList();

        logItems("NOT COOKABLE items", notCookable);

        // ================= NORMALIZE =================
        List<String> normalized = expiringRaw.stream()
                .map(Item::getProductName)
                .filter(this::isValidName)
                .filter(recipeIngredientFilter::isCookableIngredient)
                .map(this::normalize)
                .toList();

        log.info("AFTER FILTER count = {}", normalized.size());

        // ================= DISTINCT DEBUG =================
        Map<String, Integer> countMap = new HashMap<>();

        for (String name : normalized) {
            countMap.put(name, countMap.getOrDefault(name, 0) + 1);
        }

        countMap.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .forEach(e ->
                        log.warn("DUPLICATE normalized='{}' count={}", e.getKey(), e.getValue())
                );

        List<String> expiring = normalized.stream()
                .distinct()
                .toList();

        log.info("FINAL EXPIRING DISTINCT count = {}", expiring.size());

        // ================= STABLE =================
        List<String> stable = items.stream()
                .filter(item -> isStable(today, item.getExpiryDate()))
                .map(Item::getProductName)
                .filter(this::isValidName)
                .filter(recipeIngredientFilter::isCookableIngredient)
                .map(this::normalize)
                .distinct()
                .limit(MAX_STABLE_ITEMS)
                .toList();

        log.info("STABLE count = {}", stable.size());

        // ================= MERGE =================
        List<String> all = mergeIngredients(expiring, stable);

        log.info("FINAL INGREDIENTS count = {}", all.size());
        log.info("===== AGG END =====");

        return new RecipeAggregationResult(userId, expiring, stable, all);
    }

    // ================= LOG HELPER =================
    private void logItems(String label, List<Item> items) {
        if (items.isEmpty()) return;

        log.warn("{}:", label);
        items.forEach(i ->
                log.warn(" - id={} name={} expiry={}",
                        i.getId(),
                        i.getProductName(),
                        i.getExpiryDate())
        );
    }

    // ================= BUSINESS LOGIC =================

    /**
     * Rule: CHỈ lấy từ hôm nay → +3 ngày
     */
    private boolean isExpiringSoon(LocalDate today, LocalDate expiryDate) {
        if (expiryDate == null) return false;

        long days = daysBetween(today, expiryDate);

        return days >= 0 && days <= EXPIRING_DAYS_THRESHOLD;
    }

    /**
     * Dùng để debug (log ra những item bị loại)
     */
    private boolean isExpired(LocalDate today, LocalDate expiryDate) {
        if (expiryDate == null) return false;

        return daysBetween(today, expiryDate) < 0;
    }

    private boolean isStable(LocalDate today, LocalDate expiryDate) {
        if (expiryDate == null) return false;

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