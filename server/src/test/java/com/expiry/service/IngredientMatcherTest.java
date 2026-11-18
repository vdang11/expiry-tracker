package com.expiry.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class IngredientMatcherTest {

    @ParameterizedTest(name = "\"{0}\" matches \"{1}\"")
    @CsvSource({
            "milk, milk",
            "Milk, milk",
            "fresh milk, milk",
            "Fresh Milk 1L, whole milk",
            "Vinamilk Fresh Milk, milk",
            "eggs, egg",
            "egg, large eggs",
            "tomatoes, tomato",
            "cherry tomatoes, tomato",
            "berries, berry",
            "chicken breast, chicken",
            "chicken breast, boneless skinless chicken breasts",
            "spinach, baby spinach",
            "cheddar cheese, cheese",
            "green onions, green onion",
            "noodles, noodle",
            "peaches, peach",
            "hummus, hummus",
            "sữa tươi, sữa"
    })
    void should_match(String a, String b) {
        assertThat(IngredientMatcher.matches(a, b)).isTrue();
        assertThat(IngredientMatcher.matches(b, a)).isTrue();
    }

    @ParameterizedTest(name = "\"{0}\" does not match \"{1}\"")
    @CsvSource({
            "egg, eggplant",
            "corn, popcorn",
            "oil, boiled egg",
            "ham, hamburger bun",
            "pea, peanut",
            "rice, licorice",
            "green onions, green beans",
            "soy sauce, tomato sauce",
            "cheddar cheese, cream cheese",
            "chicken breast, chicken stock",
            "chicken breast, chicken thigh",
            "red apple, red onion",
            "milk, ''",
            "fresh, milk"
    })
    void should_not_match(String a, String b) {
        assertThat(IngredientMatcher.matches(a, b)).isFalse();
        assertThat(IngredientMatcher.matches(b, a)).isFalse();
    }
}
