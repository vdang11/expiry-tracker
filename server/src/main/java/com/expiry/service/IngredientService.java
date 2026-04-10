package com.expiry.service;

import com.expiry.entity.Ingredient;
import com.expiry.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    public Ingredient findOrCreate(String name) {
        return ingredientRepository.findByName(name)
                .orElseGet(() -> createIngredient(name));
    }

    private Ingredient createIngredient(String name) {
        Ingredient ingredient = Ingredient.builder()
                .name(name)
                .build();

        return ingredientRepository.save(ingredient);
    }
}