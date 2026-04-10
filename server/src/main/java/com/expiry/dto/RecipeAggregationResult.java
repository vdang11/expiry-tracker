package com.expiry.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RecipeAggregationResult {

    private Long userId;

    private List<String> expiringIngredients;

    private List<String> stableIngredients;

    private List<String> ingredients;
}