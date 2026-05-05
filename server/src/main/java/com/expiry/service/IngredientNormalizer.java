package com.expiry.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class IngredientNormalizer {

    private static final List<String> NOISE_WORDS = List.of(
            "fresh",
            "organic",
            "pure",
            "natural",
            "full",
            "cream",
            "low",
            "fat",
            "skim",
            "free",
            "range",
            "large",
            "small",
            "medium"
    );

    private static final List<String> COMPOSITE_KEYWORDS = List.of(
            "mix",
            "mixed",
            "mince",
            "blend"
    );

    private static final Map<String, String> CANONICAL_MAP = Map.ofEntries(
            Map.entry("milk", "milk"),
            Map.entry("banana", "banana"),
            Map.entry("beef", "beef"),
            Map.entry("chicken", "chicken"),
            Map.entry("pork", "pork"),
            Map.entry("egg", "egg"),
            Map.entry("eggs", "egg"),
            Map.entry("rice", "rice"),
            Map.entry("apple", "apple"),
            Map.entry("fish", "fish")
    );

    public String normalize(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return null;
        }

        String cleaned = rawName
                .toLowerCase()
                .replace("-", " ")
                .replace("_", " ")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleaned.isBlank()) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        for (String word : cleaned.split(" ")) {
            if (!NOISE_WORDS.contains(word)) {
                builder.append(word).append(" ");
            }
        }

        String result = builder.toString().trim();

        if (result.isBlank()) {
            return null;
        }

        if (isCompositeIngredient(result)) {
            return result;
        }

        for (Map.Entry<String, String> entry : CANONICAL_MAP.entrySet()) {
            if (containsWholeWord(result, entry.getKey())) {
                return entry.getValue();
            }
        }

        return result;
    }

    private boolean isCompositeIngredient(String input) {
        for (String keyword : COMPOSITE_KEYWORDS) {
            if (containsWholeWord(input, keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsWholeWord(String input, String word) {
        String paddedInput = " " + input + " ";
        String paddedWord = " " + word + " ";
        return paddedInput.contains(paddedWord);
    }
}