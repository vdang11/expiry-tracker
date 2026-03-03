package com.example.expiry.infrastructure.ai;

import com.example.expiry.dto.ExpiryResult;
import com.example.expiry.domain.ProductNameNormalizer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class OpenAIClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAIClient(RestClient openAIClient) {
        this.restClient = openAIClient;
    }

    public ExpiryResult callVision(String base64Image) {

        // =========================
        // 1) PROMPT: expiry date + product name (JSON ONLY)
        // =========================
        String prompt = """
                You are extracting information from a food product image.

                IMPORTANT SAFETY RULES:
                - Do NOT guess a random expiry date.
                - If the expiry text is unreadable/blurry, return UNKNOWN and set imageQuality="BLURRY".

                Return JSON ONLY (no markdown, no explanations) with exactly this format:
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

                Expiry rules:
                1) If you can see an explicit full date (e.g., DD/MM/YYYY, YYYY-MM-DD, MM/DD/YYYY), convert it to ISO YYYY-MM-DD,
                   set dateType="CONFIRMED", and choose confidence from 0.0 to 1.0.
                2) If you can ONLY see MONTH/YEAR (e.g., 03/2026, MAR 2026):
                   - Set expiryDate to the LAST day of that month in ISO (e.g., 2026-03-31)
                   - Set dateType="ESTIMATED"
                   - confidence MUST be < 0.6
                3) If you can ONLY see YEAR, or no expiry info: set expiryDate="UNKNOWN", dateType="UNKNOWN", confidence=0.0.

                Package rules:
                - If you can see a package/label or printed text area where expiry would normally exist, set packagePresent="YES".
                - If the item appears unpackaged / no label / no printed text area (e.g., loose fruit, cooked food on a plate), set packagePresent="NO".
                - Otherwise packagePresent="UNKNOWN".

                Estimation mode (ONLY when packagePresent="NO"):
                - You MUST NOT pretend you saw a printed expiry date.
                - Set dateType="ESTIMATED".
                - Set confidence < 0.6.
                - Choose itemCategory and freshnessState.
                - Provide a conservative estimatedShelfLifeDays (integer). If unsure, set itemCategory="UNKNOWN" and estimatedShelfLifeDays=0.

                Product rules:
                - productName: short product name only (no weight, no nutrition, no expiry text). If not found, use null.
                - productNameConfidence: 0.0 to 1.0.
                """;

        // =========================
        // 2) BODY GỬI THẬT (CÓ BASE64)
        // =========================
        Map<String, Object> body = Map.of(
                "model", "gpt-4.1-mini",
                "input", List.of(
                        Map.of(
                                "type", "message",
                                "role", "user",
                                "content", List.of(
                                        Map.of(
                                                "type", "input_text",
                                                "text", prompt
                                        ),
                                        Map.of(
                                                "type", "input_image",
                                                "image_url", "data:image/jpeg;base64," + base64Image
                                        )
                                )
                        )
                )
        );

        // =========================
        // 3) BODY LOG (KHÔNG BASE64)
        // =========================
        Map<String, Object> logBody = Map.of(
                "model", "gpt-4.1-mini",
                "input", List.of(
                        Map.of(
                                "type", "message",
                                "role", "user",
                                "content", List.of(
                                        Map.of(
                                                "type", "input_text",
                                                "text", "Vision prompt: expiryDate + productName (JSON only)"
                                        ),
                                        Map.of(
                                                "type", "input_image",
                                                "image_url", "<<BASE64_IMAGE>>"
                                        )
                                )
                        )
                )
        );

        System.out.println(">>> FINAL INPUT SENT TO OPENAI (SANITIZED) = " + logBody);

        try {
            Map<?, ?> response = restClient.post()
                    .uri("/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            System.out.println(">>> RAW OPENAI RESPONSE = " + response);

            String outputText = extractOutputText(response);
            System.out.println(">>> OUTPUT_TEXT = " + outputText);

            String json = stripMarkdownCodeFences(outputText);

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
            System.out.println(">>> OPENAI CALL FAILED");
            e.printStackTrace();

            // Sprint 2 requirement: safer fallback (do not crash request)
            return new ExpiryResult(
                    "UNKNOWN",
                    "UNKNOWN",
                    "UNKNOWN",
                    "UNKNOWN",
                    "UNKNOWN",
                    "UNKNOWN",
                    0,
                    0.0,
                    null,
                    0.0
            );
        }
    }

    private String extractOutputText(Map<?, ?> response) {
        if (response == null) return "";

        Object outputText = response.get("output_text");
        if (outputText instanceof String s && !s.isBlank()) {
            return s;
        }

        // Fallback: output[0].content[0].text
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

        // ```json ... ```
        if (trimmed.startsWith("```")) {
            // remove first line ``` or ```json
            trimmed = trimmed.replaceFirst("^```[a-zA-Z0-9]*\n?", "");
            // remove ending ```
            trimmed = trimmed.replaceFirst("\n?```$", "");
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
