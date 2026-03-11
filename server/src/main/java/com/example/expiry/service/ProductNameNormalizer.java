package com.example.expiry.service;

import java.util.Locale;
import java.util.regex.Pattern;

public final class ProductNameNormalizer {

    // Pattern nhận diện phần KHÔNG PHẢI tên thực phẩm
    private static final Pattern TRAILING_FOOD_INFO_PATTERN = Pattern.compile(
            "(\\b\\d+%\\b)|" +                          // 87%
                    "(\\b\\d+\\s?(g|kg|ml|l|oz)\\b)|" +          // 500g, 1kg, 330ml
                    "(\\bkcal\\b|\\bcal\\b)|" +                  // calories
                    "(\\bfat\\b|\\bprotein\\b|\\bsugar\\b)|" +  // nutrition
                    "(\\blean\\b)|" +
                    "(\\bexp\\b|\\bbest before\\b|\\buse by\\b)",
            Pattern.CASE_INSENSITIVE
    );

    private ProductNameNormalizer() {}

    /**
     * Normalize product name cho domain THỰC PHẨM
     *
     * @param rawProductName tên thực phẩm raw từ AI (có thể null)
     * @return tên thực phẩm đã clean hoặc null
     */
    public static String normalize(String rawProductName) {

        // 1. Null-safe
        if (rawProductName == null) {
            return null;
        }

        // 2. Dọn ký tự xuống dòng, tab
        String cleaned = rawProductName
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ")
                .trim();

        if (cleaned.isEmpty()) {
            return null;
        }

        // 3. Collapse space
        cleaned = cleaned.replaceAll("\\s{2,}", " ");

        // 4. Cắt phần dinh dưỡng / khối lượng
        String[] tokens = cleaned.split(" ");
        StringBuilder nameBuilder = new StringBuilder();

        for (String token : tokens) {
            if (TRAILING_FOOD_INFO_PATTERN.matcher(token).find()) {
                break;
            }
            nameBuilder.append(token).append(" ");
        }

        String nameOnly = nameBuilder.toString().trim();
        if (nameOnly.isEmpty()) {
            nameOnly = cleaned;
        }

        // 5. Title Case đơn giản cho food
        return toTitleCase(nameOnly);
    }

    private static String toTitleCase(String input) {
        StringBuilder result = new StringBuilder();
        String[] words = input.split(" ");

        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            if (w.length() == 0) continue;

            result.append(
                    w.substring(0, 1).toUpperCase(Locale.ROOT)
                            + w.substring(1).toLowerCase(Locale.ROOT)
            );

            if (i < words.length - 1) {
                result.append(" ");
            }
        }

        return result.toString();
    }
}
