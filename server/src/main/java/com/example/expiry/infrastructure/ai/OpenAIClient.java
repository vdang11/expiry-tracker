package com.example.expiry.infrastructure.ai;

import com.example.expiry.service.ProductNameNormalizer;
import com.example.expiry.dto.ExpiryResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class OpenAIClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAIClient.class);

    private static final String MODEL = "gpt-4.1-mini";

    private static final String PROMPT = """
            You are extracting information from one or more images of the SAME food item package.
            Different images may show different sides of the package.

            IMPORTANT: Combine information across all images before answering.

            --------------------------------
            VISION ATTENTION STRATEGY
            --------------------------------

            STEP 1 — SEARCH FOR EXPIRY KEYWORDS FIRST

            Carefully scan the images for expiry-related keywords such as:

            EXP
            EXPIRY
            BEST BEFORE
            USE BY
            BBE
            DATE

            These keywords usually appear near the expiry date.

            STEP 2 — READ THE DATE NEAR THE KEYWORD

            If a keyword is found, read the nearby date.
            The date may appear above, below, or next to the keyword.
            
            Only extract a date if ALL conditions are true:
            
            1. A clear expiry keyword is visible.
            2. A readable date appears next to that keyword.
            3. The characters forming the date are visually distinguishable.
            
            Do NOT guess numbers.
            
            Do NOT invent missing digits.
            
            Do NOT attempt to "complete" blurry numbers.
            
            If the printed characters are dot-matrix and unclear,
            you must return UNKNOWN.
            
            --------------------------------
            DOT MATRIX PRINT RULE
            --------------------------------
            
            Many expiry dates are printed with dot-matrix ink.
            
            If the dot-matrix characters are:
            
            - incomplete
            - partially missing
            - merged together
            - visually ambiguous
            
            You must treat the date as unreadable.
            
            Return:
            
            expiryDate="UNKNOWN"
            imageQuality="BLURRY"
            confidence=0.0
            
            Do NOT estimate the numbers.
            
            --------------------------------
            VALID DATE PATTERNS
            --------------------------------
            
            Common formats used in Australian packaging include:
            
            DD/MM/YYYY
            DD/MM/YY
            YYYY-MM-DD
            DD MON YY
            DD MON YYYY
            DayMonthYear format such as:
            12JAN26
            27MAR2026
            
            Examples:
            
            USE BY 27 MAR 26
            BEST BEFORE 12/09/2025
            BBE 2026-03-27
            EXP 15 JAN 25
            
            STEP 3 — ONLY AFTER EXPIRY CHECK

            Identify the FOOD PRODUCT NAME on the packaging.

            --------------------------------
            IMPORTANT SAFETY RULES
            --------------------------------

            - Do NOT guess a random expiry date if there are many numbers in the photo.
            - If the expiry text is unreadable/blurry, return UNKNOWN and set imageQuality="BLURRY".

            --------------------------------
            RETURN JSON ONLY
            --------------------------------

            {
              "expiryDate": "YYYY-MM-DD or UNKNOWN",
              "dateType": "CONFIRMED|ESTIMATED|UNKNOWN",
              "imageQuality": "OK|BLURRY|UNKNOWN",
              "packagePresent": "YES|NO|UNKNOWN",
              "itemCategory": "FRESH_PRODUCE|FRESH_MEAT|FRESH_SEAFOOD|DAIRY|BAKERY|READY_TO_EAT|PANTRY_PACKAGED|FROZEN|BEVERAGE|UNKNOWN",
              "freshnessState": "FRESH|NEW|RIPE|OPENED|COOKED|LEFTOVER|UNKNOWN",
              "estimatedShelfLifeDays": 0,
              "confidence": 0.0,
              "productName": "string or null",
              "productNameConfidence": 0.0
            }
            
            --------------------------------
            EXPIRY RULES
            --------------------------------

            1) If you can see a full expiry date (DD/MM/YYYY, YYYY-MM-DD, MM/DD/YYYY):
               convert it to ISO format YYYY-MM-DD,
               set dateType="CONFIRMED".

            2) If only MONTH/YEAR is visible (e.g. 03/2026, MAR 2026):
               set expiryDate to the LAST day of that month,
               set dateType="ESTIMATED",
               confidence MUST be < 0.6.

            3) If only YEAR or nothing is visible:
               set expiryDate="UNKNOWN",
               dateType="UNKNOWN",
               confidence=0.0.

            --------------------------------
            PACKAGE RULES
            --------------------------------

            If a package or printed label is visible → packagePresent="YES".
            If food appears unpackaged (loose fruit, cooked meal, etc.) → packagePresent="NO".
            Otherwise → packagePresent="UNKNOWN".

            --------------------------------
            ESTIMATION MODE
            --------------------------------

            ONLY when packagePresent="NO":

            - Do NOT pretend you saw a printed expiry date.
            - Set dateType="ESTIMATED".
            - confidence < 0.6.
            - Choose itemCategory and freshnessState.
            - Provide conservative estimatedShelfLifeDays.

            --------------------------------
            PRODUCT NAME RULES
            --------------------------------

            - productName: short food item name only.
            - Ignore weight, nutrition, expiry text, slogans.
            - If not found → return null.
            - productNameConfidence between 0.0 and 1.0
            """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public OpenAIClient(RestClient openAIClient, ObjectMapper objectMapper) {
        this.restClient = openAIClient;
        this.objectMapper = objectMapper;
    }

    public ExpiryResult callVision(List<VisionImage> images) {
        Map<String, Object> body = buildBody(images);
        Map<String, Object> logBody = buildLogBody(images);

        log.info("FINAL INPUT SENT TO OPENAI (SANITIZED) = {}", logBody);

        Map<?, ?> response = restClient.post()
                .uri("/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        log.info("RAW OPENAI RESPONSE = {}", response);

        String outputText = extractOutputText(response);
        if (outputText == null || outputText.isBlank()) {
            throw new IllegalStateException("OpenAI response missing output_text");
        }

        String json = stripMarkdownCodeFences(outputText);
        try {
            JsonNode node = objectMapper.readTree(json);

            String expiryDate = readText(node, "expiryDate", "UNKNOWN");
            String dateType = readText(node, "dateType", "UNKNOWN");
            String imageQuality = readText(node, "imageQuality", "UNKNOWN");
            String packagePresent = readText(node, "packagePresent", "UNKNOWN");
            String itemCategory = readText(node, "itemCategory", "UNKNOWN");
            String freshnessState = readText(node, "freshnessState", "UNKNOWN");
            Integer estimatedShelfLifeDays = readInt(node, "estimatedShelfLifeDays", 0);
            double confidence = readDouble(node, "confidence", 0.0);

            String rawProductName = readNullableText(node, "productName");
            double productNameConfidence = readDouble(node, "productNameConfidence", 0.0);

            String productName = ProductNameNormalizer.normalize(rawProductName);

            return new ExpiryResult(
                    expiryDate,
                    dateType,
                    imageQuality,
                    packagePresent,
                    itemCategory,
                    freshnessState,
                    estimatedShelfLifeDays,
                    confidence,
                    productName,
                    productNameConfidence
            );

        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse OpenAI JSON output", e);
        }
    }

    private Map<String, Object> buildBody(List<VisionImage> images) {
        List<Map<String, Object>> content = new ArrayList<>();
        content.add(Map.of("type", "input_text", "text", PROMPT));

        for (VisionImage img : images) {
            String mime = (img.mimeType() == null || img.mimeType().isBlank()) ? "image/jpeg" : img.mimeType();
            content.add(Map.of(
                    "type", "input_image",
                    "image_url", "data:" + mime + ";base64," + img.base64()
            ));
        }

        return Map.of(
                "model", MODEL,
                "input", List.of(
                        Map.of(
                                "type", "message",
                                "role", "user",
                                "content", content
                        )
                )
        );
    }

    private Map<String, Object> buildLogBody(List<VisionImage> images) {
        List<Map<String, Object>> content = new ArrayList<>();
        content.add(Map.of("type", "input_text", "text", PROMPT));

        for (int i = 0; i < images.size(); i++) {
            content.add(Map.of(
                    "type", "input_image",
                    "image_url", "BASE64_IMAGE_REDACTED"
            ));
        }

        return Map.of(
                "model", MODEL,
                "input", List.of(
                        Map.of(
                                "type", "message",
                                "role", "user",
                                "content", content
                        )
                )
        );
    }

    private String extractOutputText(Map<?, ?> response) {
        if (response == null) return "";

        Object outputText = response.get("output_text");
        if (outputText instanceof String s && !s.isBlank()) return s;

        Object outputObj = response.get("output");
        if (outputObj instanceof List<?> outputList && !outputList.isEmpty()) {
            Object first = outputList.get(0);
            if (first instanceof Map<?, ?> firstMap) {
                Object contentObj = firstMap.get("content");
                if (contentObj instanceof List<?> contentList && !contentList.isEmpty()) {
                    Object firstContent = contentList.get(0);
                    if (firstContent instanceof Map<?, ?> contentMap) {
                        Object textObj = contentMap.get("text");
                        if (textObj instanceof String s) return s;
                    }
                }
            }
        }
        return "";
    }

    private String stripMarkdownCodeFences(String text) {
        if (text == null) return "";
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```[a-zA-Z0-9]*\\n?", "");
            trimmed = trimmed.replaceFirst("\\n?```$", "");
        }
        return trimmed.trim();
    }

    private String readText(JsonNode node, String field, String defaultValue) {
        if (node == null || !node.has(field) || node.get(field).isNull()) return defaultValue;
        String v = node.get(field).asText();
        return (v == null || v.isBlank()) ? defaultValue : v;
    }

    private String readNullableText(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) return null;
        String v = node.get(field).asText();
        return (v == null || v.isBlank()) ? null : v;
    }

    private double readDouble(JsonNode node, String field, double defaultValue) {
        if (node == null || !node.has(field) || node.get(field).isNull()) return defaultValue;
        try {
            return node.get(field).asDouble();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private int readInt(JsonNode node, String field, int defaultValue) {
        if (node == null || !node.has(field) || node.get(field).isNull()) return defaultValue;
        try {
            return node.get(field).asInt();
        } catch (Exception e) {
            return defaultValue;
        }
    }
}