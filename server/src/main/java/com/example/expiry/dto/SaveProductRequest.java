package com.example.expiry.dto;

public class SaveProductRequest {

    private String productName;
    private String expiryDate;
    private double confidence;
    private String dateType;
    private String decisionStatus;
    private String suggestedAction;
    private Long userId;

    public SaveProductRequest() {
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getDateType() {
        return dateType;
    }

    public void setDateType(String dateType) {
        this.dateType = dateType;
    }

    public String getDecisionStatus() { return decisionStatus; }

    public void setDecisionStatus(String status) { this.decisionStatus = status; }

    public String getSuggestedAction() {
        return suggestedAction;
    }

    public void setSuggestedAction(String suggestedAction) {
        this.suggestedAction = suggestedAction;
    }
    public Long getUserId() { return userId; }

    public void setUserId(Long userId) { this.userId = userId; }
}