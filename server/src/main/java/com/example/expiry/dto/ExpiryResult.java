package com.example.expiry.dto;

public class ExpiryResult {

    private String expiryDate;

    /**
     * CONFIRMED | ESTIMATED | UNKNOWN
     * - CONFIRMED: model saw an explicit expiry date on the package.
     * - ESTIMATED: derived from partial date (e.g., MM/YYYY) using safe rules.
     * - UNKNOWN: unreadable / missing.
     */
    private String dateType;

    /** OK | BLURRY | UNKNOWN */
    private String imageQuality;

    /** YES | NO | UNKNOWN (whether a package/label with printed expiry is visible) */
    private String packagePresent;

    /** Classification helpers used when packagePresent=NO (estimation mode) */
    private String itemCategory;        // FRESH_PRODUCE, FRESH_MEAT, DAIRY, BAKERY, READY_TO_EAT, PANTRY_PACKAGED, FROZEN, BEVERAGE, UNKNOWN
    private String freshnessState;      // FRESH, NEW, RIPE, OPENED, COOKED, LEFTOVER, UNKNOWN
    private Integer estimatedShelfLifeDays;

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

    public ExpiryResult(String expiryDate, String dateType, String imageQuality, double confidence,
                        String productName, double productNameConfidence) {
        this.expiryDate = expiryDate;
        this.dateType = dateType;
        this.imageQuality = imageQuality;
        this.confidence = confidence;
        this.productName = productName;
        this.productNameConfidence = productNameConfidence;
    }

    public ExpiryResult(String expiryDate, String dateType, String imageQuality, String packagePresent,
                        String itemCategory, String freshnessState, Integer estimatedShelfLifeDays,
                        double confidence, String productName, double productNameConfidence) {
        this.expiryDate = expiryDate;
        this.dateType = dateType;
        this.imageQuality = imageQuality;
        this.packagePresent = packagePresent;
        this.itemCategory = itemCategory;
        this.freshnessState = freshnessState;
        this.estimatedShelfLifeDays = estimatedShelfLifeDays;
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

    public String getDateType() {
        return dateType;
    }

    public void setDateType(String dateType) {
        this.dateType = dateType;
    }

    public String getImageQuality() {
        return imageQuality;
    }

    public void setImageQuality(String imageQuality) {
        this.imageQuality = imageQuality;
    }

    public String getPackagePresent() {
        return packagePresent;
    }

    public void setPackagePresent(String packagePresent) {
        this.packagePresent = packagePresent;
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }

    public String getFreshnessState() {
        return freshnessState;
    }

    public void setFreshnessState(String freshnessState) {
        this.freshnessState = freshnessState;
    }

    public Integer getEstimatedShelfLifeDays() {
        return estimatedShelfLifeDays;
    }

    public void setEstimatedShelfLifeDays(Integer estimatedShelfLifeDays) {
        this.estimatedShelfLifeDays = estimatedShelfLifeDays;
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
