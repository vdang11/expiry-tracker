package com.expiry.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecipePromptBuilder {

    public String buildPrompt(List<String> ingredients, List<String> expiringIngredients) {

        String ingredientText = ingredients.stream()
                .map(item -> "- " + item)
                .collect(Collectors.joining("\n"));

        String expiringText = expiringIngredients.isEmpty()
                ? ""
                : expiringIngredients.stream()
                  .map(item -> "- " + item)
                  .collect(Collectors.joining("\n"));

        return """
    You are a recipe assistant.

    Generate recipe ideas in valid JSON array format.

    Available ingredients:
    %s

    EXPIRING INGREDIENTS (HIGH PRIORITY):
    %s

    CRITICAL RULE:
    - You MUST cover ALL expiring ingredients.
    - Every expiring ingredient MUST appear in at least one recipe.
    - You can generate as many recipes as needed.

    IMPORTANT RULES:
    - Use available ingredients as base.
    - Keep recipes simple and realistic.
    - Each recipe must include:
      - title
      - ingredients (array)
      - steps (3 to 5)

    OUTPUT RULES:
    - Return JSON only
    - No markdown
    - No explanation
    """.formatted(ingredientText, expiringText);
    }
}