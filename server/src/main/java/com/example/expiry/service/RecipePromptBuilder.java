package com.example.expiry.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecipePromptBuilder {

    public String buildPrompt(List<String> ingredients) {
        String ingredientText = ingredients.stream()
                .map(item -> "- " + item)
                .collect(Collectors.joining("\n"));

        return """
                You are a recipe assistant.

                Generate exactly 3 simple recipe ideas in valid JSON array format.

                Available ingredients:
                %s

                IMPORTANT RULES:
                - Use the available ingredients as the main base of each recipe.
                - You may add a small number of common pantry ingredients if needed
                  (for example: salt, pepper, sugar, oil, garlic, butter, honey).
                - Keep recipes realistic, short, and beginner-friendly.
                - Focus on quick, practical home cooking.
                - Each recipe must include:
                  - title
                  - ingredients (array)
                  - steps (array of 3 to 5 short steps)
                - If only ONE ingredient is available:
                  - still generate recipes
                  - combine it with basic pantry ingredients
                  - keep recipes simple and realistic

                STRICT FILTERING RULES:
                - DO NOT create recipes from ready-to-eat meals.
                - DO NOT create recipes from packaged baby food.
                - DO NOT create recipes from snacks such as chips, cookies, crackers, candy, or chocolate bars.
                - DO NOT create recipes from packaged drinks such as soda, cola, juice drinks, or energy drinks.
                - DO NOT treat instant or convenience meals as recipe ingredients.
                - If an item already sounds like a complete prepared dish or finished meal, ignore it.
                - Focus only on cookable ingredients such as milk, fruit, vegetables, eggs, meat, yogurt, cheese, and similar ingredients.

                OUTPUT RULES:
                - Return JSON only.
                - Return exactly this structure:
                  [
                    {
                      "title": "Recipe name",
                      "ingredients": ["item 1", "item 2"],
                      "steps": ["step 1", "step 2", "step 3"]
                    }
                  ]
                - Do not include markdown.
                - Do not include explanation text.
                """.formatted(ingredientText);
    }
}