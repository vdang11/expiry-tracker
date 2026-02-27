package com.example.expiry.service;

import java.time.LocalDate;

public class ExpiryDecisionHelper {

    // =========================
    // RESULT OBJECT (gọn trong cùng file)
    // =========================
    public static class Decision {
        private String status;
        private String reason;
        private String suggestedAction;

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

    // =========================
    // MAIN LOGIC
    // =========================
    public static Decision decide(String expiryDate, double confidence) {

        if (expiryDate == null || expiryDate.equalsIgnoreCase("UNKNOWN")) {
            return new Decision(
                    "REJECTED",
                    "NO_DATE_DETECTED",
                    "ASK_USER_RESCAN"
            );
        }

        if (!isValidDate(expiryDate)) {
            return new Decision(
                    "REJECTED",
                    "INVALID_DATE",
                    "ASK_USER_MANUAL_INPUT"
            );
        }

        if (confidence >= 0.75) {
            return new Decision(
                    "CONFIRMED",
                    "HIGH_CONFIDENCE",
                    "AUTO_ACCEPT"
            );
        }

        if (confidence >= 0.4) {
            return new Decision(
                    "REVIEW",
                    "LOW_CONFIDENCE",
                    "ASK_USER_CONFIRM"
            );
        }

        return new Decision(
                "REJECTED",
                "LOW_CONFIDENCE",
                "ASK_USER_RESCAN"
        );
    }

    // =========================
    // SMALL VALIDATOR
    // =========================
    private static boolean isValidDate(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}