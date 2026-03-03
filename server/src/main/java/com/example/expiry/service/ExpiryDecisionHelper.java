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
    public static Decision decide(String expiryDate, String dateType, String imageQuality, double confidence) {

        // Normalize inputs
        String safeExpiry = (expiryDate == null) ? "UNKNOWN" : expiryDate.trim();
        String safeType = (dateType == null) ? "UNKNOWN" : dateType.trim().toUpperCase();
        String safeQuality = (imageQuality == null) ? "UNKNOWN" : imageQuality.trim().toUpperCase();

        // 0) If the image itself is blurry, we should be conservative.
        if ("BLURRY".equals(safeQuality) && (safeExpiry.equalsIgnoreCase("UNKNOWN") || confidence < 0.6)) {
            return new Decision(
                    "REJECTED",
                    "BLURRY_IMAGE",
                    "ASK_USER_RESCAN"
            );
        }

        // 1) No expiry date detected
        if (safeExpiry.equalsIgnoreCase("UNKNOWN")) {
            return new Decision(
                    "REJECTED",
                    "NO_DATE_DETECTED",
                    "ASK_USER_RESCAN"
            );
        }

        // 2) Invalid ISO date returned
        if (!isValidDate(safeExpiry)) {
            return new Decision(
                    "REJECTED",
                    "INVALID_DATE",
                    "ASK_USER_MANUAL_INPUT"
            );
        }

        // 3) Estimated date MUST be confirmed by user (Sprint 2: estimated vs confirmed)
        if ("ESTIMATED".equals(safeType)) {
            return new Decision(
                    "REVIEW",
                    "ESTIMATED_DATE",
                    "ASK_USER_CONFIRM"
            );
        }

        // 4) Threshold definition: < 0.6 is NOT auto-accepted (Sprint 2)
        if (confidence >= 0.6) {
            return new Decision(
                    "CONFIRMED",
                    "HIGH_CONFIDENCE",
                    "AUTO_ACCEPT"
            );
        }

        // 5) Low confidence: ask user to confirm (or rescan if very low)
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

    // Backward-compatible overload (in case you have older calls)
    public static Decision decide(String expiryDate, double confidence) {
        return decide(expiryDate, "UNKNOWN", "UNKNOWN", confidence);
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