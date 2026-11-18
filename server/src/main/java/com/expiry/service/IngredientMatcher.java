package com.expiry.service;

import java.util.LinkedHashSet;
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

    private IngredientMatcher() {
    }

    public static boolean matches(String a, String b) {
        Set<String> x = keyWords(a);
        Set<String> y = keyWords(b);

        if (x.isEmpty() || y.isEmpty()) {
            return false;
        }

        return x.size() <= y.size() ? y.containsAll(x) : x.containsAll(y);
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
