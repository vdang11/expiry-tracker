package com.example.expiry.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeResponse {

    private Long id;
    private String title;
    private List<String> ingredients;
    private List<String> steps;
    private boolean fromCache;
}