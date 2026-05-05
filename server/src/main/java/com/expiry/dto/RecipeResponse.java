package com.expiry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeResponse {

    private Long id;
    private String title;
    private List<String> ingredients;
    private List<String> steps;
    private boolean fromAI;

    // Expiring items that backend confirmed this recipe covers
    private List<String> expiringIngredients;

    // Actual recipe ingredient names that matched expiring items
    // FE should use this field for highlighting
    private List<String> coveredIngredients;
}