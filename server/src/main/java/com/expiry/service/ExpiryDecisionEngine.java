package com.expiry.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ExpiryDecisionEngine {

    public static class Decision {

        private final String status;
        private final String reason;
        private final String suggestedAction;

        public Decision(String status, String reason, String suggestedAction) {
            this.status = status;
            this.reason = reason;
            this.suggestedAction = suggestedAction;
        }

        public String getStatus() {
            return status;
        }

        public String getReason() {
            return reason;
        }

        public String getSuggestedAction() {
            return suggestedAction;
        }
    }

    public Decision decide(String expiryDate, String dateType, String imageQuality, double confidence) {

        String expiry = normalize(expiryDate);
        String type = normalize(dateType).toUpperCase();
        String quality = normalize(imageQuality).toUpperCase();

        // ---------- HARD REJECT RULES ----------

        if ("UNKNOWN".equals(expiry)) {
            return reject("NO_DATE_DETECTED", "ASK_USER_RESCAN");
        }

        if ("BLURRY".equals(quality) && confidence < 0.7) {
            return reject("BLURRY_IMAGE", "ASK_USER_RESCAN");
        }

        if (!isValidIsoDate(expiry)) {
            return reject("INVALID_DATE_FORMAT", "ASK_USER_MANUAL_INPUT");
        }

        LocalDate date = LocalDate.parse(expiry);
        LocalDate today = LocalDate.now();

        if (date.isBefore(today.minusYears(5))) {
            return reject("UNREALISTIC_PAST_DATE", "ASK_USER_RESCAN");
        }

        if (date.isAfter(today.plusYears(5))) {
            return reject("UNREALISTIC_FUTURE_DATE", "ASK_USER_RESCAN");
        }

        if (date.isBefore(today)) {
            return reject("PAST_DATE_DETECTED", "ASK_USER_MANUAL_INPUT");
        }

        // ---------- REVIEW RULES ----------

        if ("ESTIMATED".equals(type)) {
            return review("ESTIMATED_DATE");
        }

        // ---------- CONFIDENCE RULES ----------

        if (confidence >= 0.75) {
            return accept();
        }

        if (confidence >= 0.4) {
            return review("LOW_CONFIDENCE");
        }

        return reject("LOW_CONFIDENCE", "ASK_USER_RESCAN");
    }

    private Decision accept() {
        return new Decision("CONFIRMED", "HIGH_CONFIDENCE", "AUTO_ACCEPT");
    }

    private Decision review(String reason) {
        return new Decision("REVIEW", reason, "ASK_USER_CONFIRM");
    }

    private Decision reject(String reason, String action) {
        return new Decision("REJECTED", reason, action);
    }

    private boolean isValidIsoDate(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String normalize(String value) {
        return (value == null || value.trim().isEmpty()) ? "UNKNOWN" : value.trim();
    }
}