package com.expiry.repository;

import com.expiry.entity.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {

    boolean existsByRecipe_IdAndIngredient_Id(Long recipeId, Long ingredientId);
}