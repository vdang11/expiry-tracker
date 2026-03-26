package com.example.expiry.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RecipeAggregationResult {

    private final Long userId;
    private final List<String> ingredients;
    private final String cacheKey;
}