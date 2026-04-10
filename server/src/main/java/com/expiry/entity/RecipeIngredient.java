package com.expiry.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "recipe_ingredients",
        indexes = {
                @Index(name = "idx_ri_recipe_id", columnList = "recipe_id"),
                @Index(name = "idx_ri_ingredient_id", columnList = "ingredient_id"),
                @Index(name = "idx_ri_recipe_ingredient", columnList = "recipe_id, ingredient_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_recipe_ingredient",
                        columnNames = {"recipe_id", "ingredient_id"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    private String quantity;

    private String unit;
}