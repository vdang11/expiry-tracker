package com.expiry.controller;

import com.expiry.dto.GenerateRecipeRequest;
import com.expiry.dto.RecipeResponse;
import com.expiry.security.CurrentUserProvider;
import com.expiry.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final CurrentUserProvider currentUserProvider;

    // ===== GENERATE RECIPES =====
    @PostMapping("/generate")
    public List<RecipeResponse> generateRecipes(
            @RequestBody(required = false) GenerateRecipeRequest request
    ) {
        Long userId = currentUserProvider.getCurrentUserId();

        List<Long> excludeRecipeIds =
                (request == null || request.getExcludeRecipeIds() == null)
                        ? List.of()
                        : request.getExcludeRecipeIds();

        return recipeService.generateRecipes(userId, excludeRecipeIds);
    }
}