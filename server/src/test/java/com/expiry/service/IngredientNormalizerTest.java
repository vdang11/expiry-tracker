package com.expiry.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IngredientNormalizerTest {

    private final IngredientNormalizer normalizer = new IngredientNormalizer();

    @Test
    void shouldNormalizeMilk() {
        assertThat(normalizer.normalize("Full Cream Milk 1L"))
                .isEqualTo("milk");
    }

    @Test
    void shouldNormalizeBanana() {
        assertThat(normalizer.normalize("Organic Banana"))
                .isEqualTo("banana");
    }

    @Test
    void shouldHandleComposite() {
        assertThat(normalizer.normalize("Pork and Beef Mince"))
                .isEqualTo("pork and beef mince");
    }

    @Test
    void shouldNotBreakEggplant() {
        assertThat(normalizer.normalize("Eggplant"))
                .isEqualTo("eggplant");
    }

    @Test
    void shouldNotBreakFishcake() {
        assertThat(normalizer.normalize("Fishcake"))
                .isEqualTo("fishcake");
    }

    @Test
    void shouldReturnNullForGarbage() {
        assertThat(normalizer.normalize("!!!@@@"))
                .isNull();
    }
}