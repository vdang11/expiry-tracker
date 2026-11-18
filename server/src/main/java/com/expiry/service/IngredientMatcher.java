package com.expiry.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Decides whether two ingredient names refer to the same ingredient.
 *
 * Rule: compare whole words, not substrings. After cleanup, the words of the
 * shorter name must all appear in the longer one.
 *   "milk"           ~ "fresh whole milk"  (match)
 *   "chicken breast" ~ "chicken"           (match)
 *   "egg"            ~ "eggplant"          (no match: different word)
 *   "soy sauce"      ~ "tomato sauce"      (no match: "soy" missing)
 *
 * Exception: some multi-word names are a different food than their last
 * word ("water spinach" is not spinach, "peanut butter" is not butter).
 * A name only matches such a compound if it contains the whole compound.
 */
public final class IngredientMatcher {

    // Words that describe an ingredient without changing what it is
    private static final Set<String> DESCRIPTORS = Set.of(
            "fresh", "organic", "natural", "pure",
            "large", "medium", "small", "baby",
            "whole", "raw", "ripe", "frozen",
            "chopped", "sliced", "diced", "minced", "grated", "shredded",
            "boneless", "skinless",
            // package units left over from scanned product names
            "l", "ml", "g", "kg", "oz", "lb", "lbs", "pack", "pcs", "pc"
    );

    // Written in the singular form produced by keyWords()
    private static final List<List<String>> DISTINCT_COMPOUNDS = compounds(
            "water spinach", "sweet potato", "green bean", "bean sprout",
            "green onion", "spring onion",
            "peanut butter",
            "coconut milk", "soy milk", "almond milk", "oat milk", "rice milk",
            "coconut cream", "ice cream", "sour cream", "cream cheese",
            "coconut water",
            "fish sauce", "soy sauce", "oyster sauce", "hoisin sauce",
            "tomato sauce", "chili sauce",
            "tomato paste",
            "egg noodle", "rice noodle", "rice paper", "rice vinegar", "rice wine",
            "rice flour",
            "chicken stock", "chicken broth", "beef stock", "beef broth",
            "fish stock", "vegetable stock",
            "garlic powder", "onion powder", "chili powder",
            "baking soda", "baking powder",
            "lemon juice", "lime juice", "orange juice", "apple juice"
    );

    private IngredientMatcher() {
    }

    public static boolean matches(String a, String b) {
        Set<String> x = keyWords(a);
        Set<String> y = keyWords(b);

        if (x.isEmpty() || y.isEmpty()) {
            return false;
        }

        Set<String> shorter = x.size() <= y.size() ? x : y;
        Set<String> longer = shorter == x ? y : x;

        if (!longer.containsAll(shorter)) {
            return false;
        }

        // "spinach" must not match "water spinach"
        List<String> shortWords = new ArrayList<>(shorter);
        List<String> longWords = new ArrayList<>(longer);

        for (List<String> compound : DISTINCT_COMPOUNDS) {
            if (containsPhrase(longWords, compound) && !containsPhrase(shortWords, compound)) {
                return false;
            }
        }

        return true;
    }

    private static boolean containsPhrase(List<String> words, List<String> phrase) {
        for (int i = 0; i + phrase.size() <= words.size(); i++) {
            if (words.subList(i, i + phrase.size()).equals(phrase)) {
                return true;
            }
        }
        return false;
    }

    private static List<List<String>> compounds(String... names) {
        return java.util.Arrays.stream(names)
                .map(n -> List.of(n.split(" ")))
                .toList();
    }

    static Set<String> keyWords(String name) {
        Set<String> words = new LinkedHashSet<>();

        if (name == null) {
            return words;
        }

        // Keep letters of any language (e.g. Vietnamese), drop punctuation and numbers
        String cleaned = name.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\s]", " ")
                .trim();

        if (cleaned.isEmpty()) {
            return words;
        }

        for (String word : cleaned.split("\\s+")) {
            if (!DESCRIPTORS.contains(word)) {
                words.add(singular(word));
            }
        }

        return words;
    }

    // Simple English plural -> singular; good enough for ingredient names
    private static String singular(String w) {
        if (w.length() <= 3 || w.endsWith("ss") || w.endsWith("us")) {
            return w;
        }
        if (w.endsWith("ies")) {
            return w.substring(0, w.length() - 3) + "y";          // berries -> berry
        }
        if (w.endsWith("oes")
                || w.endsWith("ches") || w.endsWith("shes")
                || w.endsWith("xes") || w.endsWith("sses")) {
            return w.substring(0, w.length() - 2);                // tomatoes, peaches
        }
        if (w.endsWith("s")) {
            return w.substring(0, w.length() - 1);                // eggs, onions
        }
        return w;
    }
}
