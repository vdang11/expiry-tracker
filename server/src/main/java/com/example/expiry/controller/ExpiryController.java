package com.example.expiry.controller;

import com.example.expiry.dto.ExpiryResult;
import com.example.expiry.dto.ScanResponse;
import com.example.expiry.service.ScanExpiryService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/vision")
public class ExpiryController {

    private final ScanExpiryService scanExpiryService;

    public ExpiryController(ScanExpiryService scanExpiryService) {
        this.scanExpiryService = scanExpiryService;
    }

    @PostMapping("/scan")
    public ScanResponse scan(@RequestParam("images") List<MultipartFile> images) {

        ExpiryResult result = scanExpiryService.scan(images);

        String expiryDate = (result.getExpiryDate() == null) ? "UNKNOWN" : result.getExpiryDate();
        String productName = (result.getProductName() == null) ? "" : result.getProductName();

        String status = (result.getStatus() == null) ? "REJECTED" : result.getStatus();
        boolean needsUserReview = "REVIEW".equals(status);

        String message = buildMessage(result, expiryDate, needsUserReview);

        return new ScanResponse(
                productName,
                expiryDate,
                result.isProductNameAccepted(),
                needsUserReview,
                message
        );
    }

    private String buildMessage(ExpiryResult result, String expiryDate, boolean needsUserReview) {
        String status = (result.getStatus() == null) ? "" : result.getStatus();
        String reason = (result.getReason() == null) ? "" : result.getReason();

        if ("REJECTED".equals(status) && "AI_ERROR".equals(reason)) {
            return "Scan failed due to AI error. Please try again with a clearer photo.";
        }

        if ("UNKNOWN".equals(expiryDate)) {
            return "No expiry date detected. Please take a clearer photo of the expiry label or enter manually.";
        }

        if (needsUserReview) {
            return "Low confidence or estimated date. Please verify before saving.";
        }

        return "Expiry date detected successfully.";
    }
}