package com.expiry.repository;

import com.expiry.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Query("""
    SELECT DISTINCT r FROM Recipe r
    JOIN RecipeIngredient ri ON r.id = ri.recipe.id
    JOIN Ingredient i ON ri.ingredient.id = i.id
    WHERE r.user.id = :userId
    AND i.name IN :ingredients
    AND (:excludeIds IS NULL OR r.id NOT IN :excludeIds)
    """)
    List<Recipe> findReusableRecipes(Long userId,
                                     List<String> ingredients,
                                     List<Long> excludeIds);
}