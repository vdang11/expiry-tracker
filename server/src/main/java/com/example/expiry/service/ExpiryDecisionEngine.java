package com.example.expiry.service;

import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class ExpiryDecisionEngine {

    public static class Decision {
        private String status;
        private String reason;
        private String suggestedAction;

        public Decision(String status, String reason, String suggestedAction) {
            this.status = status;
            this.reason = reason;
            this.suggestedAction = suggestedAction;
        }

        public String getStatus() { return status; }
        public String getReason() { return reason; }
        public String getSuggestedAction() { return suggestedAction; }
    }

    public Decision decide(String expiryDate, String dateType, String imageQuality, double confidence) {

        String safeExpiry = (expiryDate == null) ? "UNKNOWN" : expiryDate.trim();
        String safeType = (dateType == null) ? "UNKNOWN" : dateType.trim().toUpperCase();
        String safeQuality = (imageQuality == null) ? "UNKNOWN" : imageQuality.trim().toUpperCase();

        if ("BLURRY".equals(safeQuality) && (safeExpiry.equalsIgnoreCase("UNKNOWN") || confidence < 0.6)) {
            return new Decision("REJECTED", "BLURRY_IMAGE", "ASK_USER_RESCAN");
        }

        if (safeExpiry.equalsIgnoreCase("UNKNOWN")) {
            return new Decision("REJECTED", "NO_DATE_DETECTED", "ASK_USER_RESCAN");
        }

        if (!isValidDate(safeExpiry)) {
            return new Decision("REJECTED", "INVALID_DATE", "ASK_USER_MANUAL_INPUT");
        }

        if ("ESTIMATED".equals(safeType)) {
            return new Decision("REVIEW", "ESTIMATED_DATE", "ASK_USER_CONFIRM");
        }

        if (confidence >= 0.6) {
            return new Decision("CONFIRMED", "HIGH_CONFIDENCE", "AUTO_ACCEPT");
        }

        if (confidence >= 0.4) {
            return new Decision("REVIEW", "LOW_CONFIDENCE", "ASK_USER_CONFIRM");
        }

        return new Decision("REJECTED", "LOW_CONFIDENCE", "ASK_USER_RESCAN");
    }

    private boolean isValidDate(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}