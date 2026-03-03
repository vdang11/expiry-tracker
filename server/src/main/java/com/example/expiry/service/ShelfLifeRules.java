package com.example.expiry.service;

import java.util.Locale;

/**
 when there is NO packaging / printed expiry date,
 * - This is always ESTIMATED and must be confirmed by user
 */
public class ShelfLifeRules {

    private ShelfLifeRules() {}

    /**
     * @return estimated shelf-life days, or -1 if unknown/unsupported.
     */
    public static int estimateDays(String itemCategory, String freshnessState) {
        String cat = norm(itemCategory);
        String fresh = norm(freshnessState);

        if (cat.isEmpty() || "UNKNOWN".equals(cat)) return -1;

        // Handle states that should always be short.
        if ("LEFTOVER".equals(fresh)) return 2;
        if ("COOKED".equals(fresh)) return 2;
        if ("OPENED".equals(fresh)) return 3;

        // Conservative defaults by category.
        return switch (cat) {
            case "FRESH_MEAT" -> 2;
            case "FRESH_SEAFOOD" -> 1;
            case "FRESH_PRODUCE" -> ("RIPE".equals(fresh) ? 3 : 5);
            case "DAIRY" -> 5;
            case "BAKERY" -> 3;
            case "READY_TO_EAT" -> 2;
            case "BEVERAGE" -> 7;
            case "FROZEN" -> 30;
            case "PANTRY_PACKAGED" -> 14;
            default -> -1;
        };
    }

    private static String norm(String s) {
        if (s == null) return "";
        String t = s.trim();
        if (t.isEmpty()) return "";
        return t.toUpperCase(Locale.ROOT);
    }
}
