package com.example.expiry.dto;

public record ScanResponse(
        String productName,
        String expiryDate,
        boolean productNameAccepted,
        boolean needsUserReview,
        String message
) {}