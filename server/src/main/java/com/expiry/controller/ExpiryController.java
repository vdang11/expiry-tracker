package com.expiry.controller;

import com.expiry.dto.ExpiryResult;
import com.expiry.dto.ScanResponse;
import com.expiry.security.CurrentUserProvider;
import com.expiry.service.ScanExpiryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/vision")
@Slf4j
public class ExpiryController {

    private final ScanExpiryService scanExpiryService;
    private final CurrentUserProvider currentUserProvider;

    public ExpiryController(
            ScanExpiryService scanExpiryService,
            CurrentUserProvider currentUserProvider
    ) {
        this.scanExpiryService = scanExpiryService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping(value = "/scan", consumes = "multipart/form-data")
    public ScanResponse scan(@RequestParam("images") List<MultipartFile> images) {

        Long userId = currentUserProvider.getCurrentUserId();

        log.debug("Scan request from userId={}", userId);

        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("No images uploaded");
        }

        ExpiryResult result = scanExpiryService.scan(images);

        String expiryDate =
                (result.getExpiryDate() == null) ? "UNKNOWN" : result.getExpiryDate();

        String productName =
                (result.getProductName() == null) ? "" : result.getProductName();

        String status =
                (result.getStatus() == null) ? "REJECTED" : result.getStatus();

        boolean needsUserReview = "REVIEW".equals(status);

        String message = buildMessage(result, needsUserReview);

        return new ScanResponse(
                productName,
                expiryDate,
                result.isProductNameAccepted(),
                needsUserReview,
                message
        );
    }

    private String buildMessage(ExpiryResult result, boolean needsUserReview) {

        String status = result.getStatus() == null ? "" : result.getStatus();
        String reason = result.getReason() == null ? "" : result.getReason();

        if ("REJECTED".equals(status)) {

            if ("PAST_DATE_DETECTED".equals(reason)) {
                return "Detected expiry date is already expired. Please check the label or enter manually.";
            }

            if ("UNREALISTIC_PAST_DATE".equals(reason) ||
                    "UNREALISTIC_FUTURE_DATE".equals(reason)) {
                return "Detected expiry date seems invalid. Please rescan the label.";
            }

            if ("BLURRY_IMAGE".equals(reason)) {
                return "Image is blurry. Please take a clearer photo of the expiry label.";
            }

            if ("NO_DATE_DETECTED".equals(reason)) {
                return "No expiry date detected. Please take a clearer photo or enter manually.";
            }

            if ("AI_ERROR".equals(reason)) {
                return "Scan failed due to AI error. Please try again.";
            }

            return "Could not verify expiry date. Please rescan.";
        }

        if (needsUserReview) {

            if ("ESTIMATED_DATE".equals(reason)) {
                return "Estimated expiry date detected. Please verify before saving.";
            }

            return "Low confidence expiry detection. Please verify.";
        }

        return "Expiry date detected successfully.";
    }
}