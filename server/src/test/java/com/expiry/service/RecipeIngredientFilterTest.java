package com.expiry.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeIngredientFilterTest {

    private final RecipeIngredientFilter filter = new RecipeIngredientFilter();

    @Test
    void shouldAcceptBasicIngredient() {
        assertThat(filter.isCookableIngredient("milk")).isTrue();
    }

    @Test
    void shouldRejectSnack() {
        assertThat(filter.isCookableIngredient("snack bar")).isFalse();
    }

    @Test
    void shouldRejectReadyMeal() {
        assertThat(filter.isCookableIngredient("chicken curry meal")).isFalse();
    }

    @Test
    void shouldRejectDrink() {
        assertThat(filter.isCookableIngredient("coke")).isFalse();
    }

    @Test
    void shouldRejectNull() {
        assertThat(filter.isCookableIngredient(null)).isFalse();
    }
}