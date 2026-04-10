package com.expiry.infrastructure.ai;

import com.expiry.dto.ExpiryResult;
import com.expiry.service.ProductNameNormalizer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAIClient {

    private static final String MODEL = "gpt-4.1-mini";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    // ================= VISION =================
    public ExpiryResult callVision(List<VisionImage> images, String prompt) {
        Map<String, Object> body = buildVisionRequest(images, prompt);

        Map<?, ?> response = callOpenAI(body);

        String json = extractJson(response);

        return parseExpiryResult(json);
    }

    // ================= TEXT =================
    public String generateText(String prompt) {
        Map<String, Object> body = Map.of(
                "model", MODEL,
                "input", List.of(
                        Map.of(
                                "type", "message",
                                "role", "user",
                                "content", List.of(
                                        Map.of("type", "input_text", "text", prompt)
                                )
                        )
                )
        );

        Map<?, ?> response = callOpenAI(body);

        return extractJson(response);
    }

    // ================= CORE =================
    private Map<?, ?> callOpenAI(Map<String, Object> body) {
        return restClient.post()
                .uri("/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    private String extractJson(Map<?, ?> response) {
        String text = extractOutputText(response);

        if (text == null || text.isBlank()) {
            throw new IllegalStateException("OpenAI response missing output");
        }

        return stripMarkdown(text);
    }

    // ================= BUILD REQUEST =================
    private Map<String, Object> buildVisionRequest(List<VisionImage> images, String prompt) {
        List<Map<String, Object>> content = new ArrayList<>();

        content.add(Map.of("type", "input_text", "text", prompt));

        for (VisionImage img : images) {
            content.add(Map.of(
                    "type", "input_image",
                    "image_url", buildImageUrl(img)
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

    private String buildImageUrl(VisionImage img) {
        String mime = (img.mimeType() == null || img.mimeType().isBlank())
                ? "image/jpeg"
                : img.mimeType();

        return "data:" + mime + ";base64," + img.base64();
    }

    // ================= PARSE =================
    private ExpiryResult parseExpiryResult(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);

            String expiryDate = getText(node, "expiryDate", "UNKNOWN");
            String dateType = getText(node, "dateType", "UNKNOWN");
            String imageQuality = getText(node, "imageQuality", "UNKNOWN");
            String packagePresent = getText(node, "packagePresent", "UNKNOWN");
            String itemCategory = getText(node, "itemCategory", "UNKNOWN");
            String freshnessState = getText(node, "freshnessState", "UNKNOWN");

            int estimatedDays = getInt(node, "estimatedShelfLifeDays", 0);
            double confidence = getDouble(node, "confidence", 0.0);

            String rawName = getNullableText(node, "productName");
            double nameConfidence = getDouble(node, "productNameConfidence", 0.0);

            String productName = ProductNameNormalizer.normalize(rawName);

            return new ExpiryResult(
                    expiryDate,
                    dateType,
                    imageQuality,
                    packagePresent,
                    itemCategory,
                    freshnessState,
                    estimatedDays,
                    confidence,
                    productName,
                    nameConfidence
            );

        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse OpenAI JSON", e);
        }
    }

    // ================= SAFE READ =================
    private String getText(JsonNode node, String field, String def) {
        if (!node.has(field) || node.get(field).isNull()) return def;

        String val = node.get(field).asText();
        return (val == null || val.isBlank()) ? def : val;
    }

    private String getNullableText(JsonNode node, String field) {
        if (!node.has(field) || node.get(field).isNull()) return null;

        String val = node.get(field).asText();
        return (val == null || val.isBlank()) ? null : val;
    }

    private double getDouble(JsonNode node, String field, double def) {
        try {
            return node.has(field) ? node.get(field).asDouble() : def;
        } catch (Exception e) {
            return def;
        }
    }

    private int getInt(JsonNode node, String field, int def) {
        try {
            return node.has(field) ? node.get(field).asInt() : def;
        } catch (Exception e) {
            return def;
        }
    }

    // ================= RESPONSE UTILS =================
    private String extractOutputText(Map<?, ?> response) {
        if (response == null) return "";

        if (response.get("output_text") instanceof String s && !s.isBlank()) {
            return s;
        }

        try {
            List<?> output = (List<?>) response.get("output");
            Map<?, ?> first = (Map<?, ?>) output.get(0);
            List<?> content = (List<?>) first.get("content");
            Map<?, ?> textObj = (Map<?, ?>) content.get(0);

            return (String) textObj.get("text");
        } catch (Exception e) {
            return "";
        }
    }

    private String stripMarkdown(String text) {
        String t = text.trim();

        if (t.startsWith("```")) {
            t = t.replaceFirst("^```[a-zA-Z0-9]*\\n?", "");
            t = t.replaceFirst("\\n?```$", "");
        }

        return t.trim();
    }
}