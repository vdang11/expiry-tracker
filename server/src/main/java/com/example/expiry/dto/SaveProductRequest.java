package com.example.expiry.dto;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaveProductRequest {

    private String productName;
    private String expiryDate;
    private double confidence;
    private String dateType;
    private String decisionStatus;
    private String suggestedAction;
    private Long userId;

}