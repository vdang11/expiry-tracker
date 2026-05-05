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
                ? "None"
                : expiringIngredients.stream()
                  .map(item -> "- " + item)
                  .collect(Collectors.joining("\n"));

        return """
You are a smart cooking assistant.

Your goal is to generate diverse, simple, real-life recipes.

====================
AVAILABLE INGREDIENTS:
%s

EXPIRING INGREDIENTS (HIGH PRIORITY):
%s
====================

CRITICAL RULES:
- You MUST use expiring ingredients.
- ALL expiring ingredients MUST be used across the generated recipes (not necessarily in one recipe).
- Each recipe MUST include at least one expiring ingredient.
- You can generate multiple recipes if needed to cover all expiring ingredients.

CUISINE DIVERSITY RULE (VERY IMPORTANT):
- Generate recipes from DIFFERENT cuisines.
- Avoid repeating the same cuisine style.
- Mix from:
  - Asian (Vietnamese, Chinese, Japanese, Thai, Korean)
  - European (Italian, French, Spanish)
  - American (BBQ, comfort food, sandwiches)
  - African (Moroccan, Ethiopian)
  - Other simple global home-style dishes
- Each recipe should feel culturally different.

QUALITY RULES:
- Recipes must be realistic and commonly cooked dishes.
- Do NOT combine ingredients in strange or unnatural ways.
- Prefer simple home cooking style.
- Each recipe should use 3–7 ingredients max.

STRUCTURE RULES:
Each recipe MUST include:
- title
- ingredients (array of strings)
- steps (3 to 5 short steps)

OUTPUT RULES:
- Return ONLY valid JSON array
- No markdown
- No explanation
- No extra text

EXAMPLE FORMAT:
[
  {
    "title": "Example dish",
    "ingredients": ["ingredient1", "ingredient2"],
    "steps": ["step 1", "step 2"]
  }
]
""".formatted(ingredientText, expiringText);
    }
}