package com.expiry.dto;

import com.expiry.entity.ItemCategory;
import lombok.*;
import com.expiry.service.ExpiryStatus;

@Getter
@AllArgsConstructor
public class ItemResponse {

    private Long id;
    private String productName;
    private String expiryDate;
    private double confidence;
    private String dateType;
    private String decisionStatus;
    private String suggestedAction;
    private ExpiryStatus expiryStatus;
    private String itemStatus;
    private Long daysLeft;
    private ItemCategory category;      // null = not classified yet
    private String ingredientName;
}