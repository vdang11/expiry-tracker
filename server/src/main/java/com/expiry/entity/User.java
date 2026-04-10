package com.expiry.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String name;

    @Column(nullable = false)
    private String password;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<Item> items = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<Recipe> recipes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}