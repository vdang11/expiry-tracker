package com.example.expiry.service;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RecipeCacheKeyBuilder {

    private static final List<String> STOP_WORDS = List.of(
            "fresh", "organic", "premium", "full cream", "low fat", "skim", "pure"
    );

    private static final Map<String, String> KEYWORD_MAP = Map.of(
            "milk", "milk",
            "egg", "egg",
            "chicken", "chicken",
            "beef", "beef",
            "pork", "pork",
            "rice", "rice",
            "bread", "bread",
            "cheese", "cheese",
            "banana", "banana"
    );

    public String build(List<String> ingredientNames) {
        List<String> normalized = ingredientNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toLowerCase)
                .map(this::normalizeName)
                .filter(name -> !name.isBlank())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        return String.join("|", normalized);
    }

    private String normalizeName(String raw) {
        String name = raw.toLowerCase();

        // 1. remove noise
        for (String stop : STOP_WORDS) {
            name = name.replace(stop, "");
        }

        name = name.trim().replaceAll("\\s+", " ");

        // 2. match keyword
        for (Map.Entry<String, String> entry : KEYWORD_MAP.entrySet()) {
            if (name.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return name;
    }
}