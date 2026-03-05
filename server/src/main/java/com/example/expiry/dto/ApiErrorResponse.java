package com.example.expiry.dto;

public record ApiErrorResponse(
        String code,
        String message
) {}