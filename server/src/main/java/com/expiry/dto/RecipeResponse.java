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
    private List<String> expiringIngredients;
}