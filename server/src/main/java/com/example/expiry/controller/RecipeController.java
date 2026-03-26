package com.example.expiry.controller;

import com.example.expiry.dto.RecipeResponse;
import com.example.expiry.service.RecipeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/generate")
    public List<RecipeResponse> generateRecipes(@RequestParam Long userId) {
        return recipeService.generateRecipes(userId);
    }
}