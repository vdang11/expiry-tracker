package com.expiry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "recipes",
        indexes = {
                @Index(name = "idx_recipe_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== RELATION =====
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ===== BASIC FIELDS =====
    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String ingredients;

    @Column(length = 5000)
    private String steps;

    @OneToMany(
            mappedBy = "recipe",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<RecipeIngredient> recipeIngredients = new ArrayList<>();

    private LocalDateTime createdAt;

    // ===== LIFECYCLE =====
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}