package com.example.expiry.dto;

public class ExpiryResult {

    private String expiryDate;
    private double confidence;
    private String productName;
    private double productNameConfidence;
    private String status;
    private String reason;
    private String suggestedAction;
    private boolean productNameAccepted;

    public ExpiryResult() {}

    public ExpiryResult(String expiryDate, double confidence, String productName, double productNameConfidence) {
        this.expiryDate = expiryDate;
        this.confidence = confidence;
        this.productName = productName;
        this.productNameConfidence = productNameConfidence;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductNameConfidence() {
        return productNameConfidence;
    }

    public void setProductNameConfidence(double productNameConfidence) {
        this.productNameConfidence = productNameConfidence;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSuggestedAction() {
        return suggestedAction;
    }

    public void setSuggestedAction(String suggestedAction) {
        this.suggestedAction = suggestedAction;
    }

    public boolean isProductNameAccepted() {
        return productNameAccepted;
    }

    public void setProductNameAccepted(boolean productNameAccepted) {
        this.productNameAccepted = productNameAccepted;
    }
}
