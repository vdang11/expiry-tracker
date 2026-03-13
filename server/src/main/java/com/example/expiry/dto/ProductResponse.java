package com.example.expiry.dto;

public class ProductResponse {

    private Long id;
    private String productName;
    private String expiryDate;
    private double confidence;
    private String dateType;
    private String decisionStatus;
    private String suggestedAction;

    public ProductResponse() {
    }

    public ProductResponse(Long id, String productName, String expiryDate,
                           double confidence, String dateType,
                           String decisionStatus, String suggestedAction) {
        this.id = id;
        this.productName = productName;
        this.expiryDate = expiryDate;
        this.confidence = confidence;
        this.dateType = dateType;
        this.decisionStatus = decisionStatus;
        this.suggestedAction = suggestedAction;
    }

    public Long getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getDateType() {
        return dateType;
    }

    public String getDecisionStatus() {
        return decisionStatus;
    }

    public String getSuggestedAction() {
        return suggestedAction;
    }
}