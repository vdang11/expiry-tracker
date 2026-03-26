package com.example.expiry.repository;

import com.example.expiry.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findAllByCacheKey(String cacheKey);
}