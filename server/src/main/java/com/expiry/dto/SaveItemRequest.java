package com.expiry.dto;

import com.expiry.entity.ItemCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveItemRequest {
    private String productName;
    private String expiryDate;
    private Double confidence;
    private String dateType;
    private String decisionStatus;
    private String suggestedAction;

    // Optional: chosen by the user; null = let the AI decide
    private ItemCategory category;
}