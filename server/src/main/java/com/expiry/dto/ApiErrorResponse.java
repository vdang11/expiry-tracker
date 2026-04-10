package com.expiry.dto;

public record ApiErrorResponse(
        String code,
        String message
) {}