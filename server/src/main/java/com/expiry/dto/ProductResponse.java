package com.expiry.dto;
import lombok.*;
import com.expiry.service.ExpiryStatus;

@Getter
@AllArgsConstructor
public class ProductResponse {

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
}