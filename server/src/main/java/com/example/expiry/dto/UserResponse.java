package com.example.expiry.dto;

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private LocalDateTime createdAt;

    public UserResponse() {
    }

    public UserResponse(Long id, String email, String name, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}