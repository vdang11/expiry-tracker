package com.example.expiry.dto;

import com.example.expiry.model.ExpiryStatus;

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

    public ProductResponse(Long id,
                           String productName,
                           String expiryDate,
                           double confidence,
                           String dateType,
                           String decisionStatus,
                           String suggestedAction,
                           ExpiryStatus expiryStatus,
                           String itemStatus,
                           Long daysLeft) {

        this.id = id;
        this.productName = productName;
        this.expiryDate = expiryDate;
        this.confidence = confidence;
        this.dateType = dateType;
        this.decisionStatus = decisionStatus;
        this.suggestedAction = suggestedAction;
        this.expiryStatus = expiryStatus;
        this.itemStatus = itemStatus;
        this.daysLeft = daysLeft;
    }

    public Long getId() { return id; }
    public String getProductName() { return productName; }
    public String getExpiryDate() { return expiryDate; }
    public double getConfidence() { return confidence; }
    public String getDateType() { return dateType; }
    public String getDecisionStatus() { return decisionStatus; }
    public String getSuggestedAction() { return suggestedAction; }
    public ExpiryStatus getExpiryStatus() { return expiryStatus; }
    public String getItemStatus() { return itemStatus; }
    public Long getDaysLeft() { return daysLeft; }
}