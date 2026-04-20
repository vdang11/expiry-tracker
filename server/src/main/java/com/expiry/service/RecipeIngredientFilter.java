package com.expiry.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class RecipeIngredientFilter {

    private static final List<String> EXCLUDED_KEYWORDS = List.of(
            // ready-to-eat / ready meals
            "ready meal",
            "ready-to-eat",
            "ready to eat",
            "microwave meal",
            "frozen dinner",
            "complete meal",
            "meal bowl",
            "meal pouch",
            "baby food",
            "baby meal",
            "toddler meal",
            "puree pouch",
            "food pouch",

            // snacks / sweets
            "snack",
            "chips",
            "crisps",
            "cookie",
            "cookies",
            "biscuit",
            "biscuits",
            "cracker",
            "crackers",
            "candy",
            "chocolate",
            "lollipop",
            "marshmallow",
            "gummy",
            "popcorn",
            "granola bar",
            "protein bar",
            "snack bar",

            // packaged drinks
            "soft drink",
            "soda",
            "cola",
            "energy drink",
            "sports drink",
            "juice drink",
            "fruit drink",
            "iced tea",
            "bottled tea",
            "coke",
            "sprite",
            "fanta",
            

            // instant / heavily processed convenience food
            "instant noodle",
            "instant noodles",
            "cup noodle",
            "cup noodles",
            "ramen cup",
            "instant soup",
            "ready pasta",
            "premade meal",

            // supplements / meal replacement
            "meal replacement",
            "nutrition shake",
            "protein shake",
            "supplement",
            "formula"
    );

    public boolean isCookableIngredient(String productName) {
        if (productName == null || productName.isBlank()) {
            return false;
        }

        String normalized = normalize(productName);

        for (String keyword : EXCLUDED_KEYWORDS) {
            if (normalized.contains(keyword)) {
                return false;
            }
        }

        if (looksLikeReadyMeal(normalized)) {
            return false;
        }

        return true;
    }

    private boolean looksLikeReadyMeal(String normalized) {
        // ví dụ:
        // "chicken coconut quinoa turmeric"
        // "beef rice vegetable meal"
        // "salmon pasta bake"
        // nếu tên dài + có nhiều từ mô tả món ăn hoàn chỉnh thì loại.
        boolean hasMealWord =
                normalized.contains(" meal") ||
                        normalized.contains(" dinner") ||
                        normalized.contains(" lunch") ||
                        normalized.contains(" breakfast") ||
                        normalized.contains(" bowl") ||
                        normalized.contains(" risotto") ||
                        normalized.contains(" pasta") ||
                        normalized.contains(" curry") ||
                        normalized.contains("stew") ||
                        normalized.contains(" soup");

        int wordCount = normalized.split("\\s+").length;

        return hasMealWord && wordCount >= 3;
    }

    private String normalize(String input) {
        return input
                .toLowerCase(Locale.ROOT)
                .replace("-", " ")
                .replace("_", " ")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}