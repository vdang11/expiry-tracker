package com.expiry.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpiryResult {

    private String expiryDate;

    /**
     * CONFIRMED | ESTIMATED | UNKNOWN
     */
    private String dateType;

    /** OK | BLURRY | UNKNOWN */
    private String imageQuality;

    /** YES | NO | UNKNOWN */
    private String packagePresent;

    /** Classification helpers */
    private String itemCategory;
    private String freshnessState;
    private Integer estimatedShelfLifeDays;

    private double expiryConfidence;
    private String productName;
    private double productNameConfidence;

    private String status;
    private String reason;
    private String suggestedAction;

    private boolean productNameAccepted;

    // ===== Custom constructors (GIỮ LẠI) =====

    public ExpiryResult(String expiryDate, double confidence,
                        String productName, double productNameConfidence) {
        this.expiryDate = expiryDate;
        this.expiryConfidence = confidence;
        this.productName = productName;
        this.productNameConfidence = productNameConfidence;
    }

    public ExpiryResult(String expiryDate, String dateType, String imageQuality,
                        double confidence, String productName, double productNameConfidence) {
        this.expiryDate = expiryDate;
        this.dateType = dateType;
        this.imageQuality = imageQuality;
        this.expiryConfidence = confidence;
        this.productName = productName;
        this.productNameConfidence = productNameConfidence;
    }

    public ExpiryResult(String expiryDate, String dateType, String imageQuality,
                        String packagePresent, String itemCategory, String freshnessState,
                        Integer estimatedShelfLifeDays, double confidence,
                        String productName, double productNameConfidence) {

        this.expiryDate = expiryDate;
        this.dateType = dateType;
        this.imageQuality = imageQuality;
        this.packagePresent = packagePresent;
        this.itemCategory = itemCategory;
        this.freshnessState = freshnessState;
        this.estimatedShelfLifeDays = estimatedShelfLifeDays;
        this.expiryConfidence = confidence;
        this.productName = productName;
        this.productNameConfidence = productNameConfidence;
    }
}