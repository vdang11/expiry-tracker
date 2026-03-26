package com.example.expiry.infrastructure.ai;

import com.example.expiry.dto.ExpiryResult;
import com.example.expiry.service.ProductNameNormalizer;
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

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public OpenAIClient(RestClient openAIClient, ObjectMapper objectMapper) {
        this.restClient = openAIClient;
        this.objectMapper = objectMapper;
    }

    public ExpiryResult callVision(List<VisionImage> images, String prompt) {
        Map<String, Object> body = buildVisionBody(images, prompt);
        Map<String, Object> logBody = buildVisionLogBody(images, prompt);

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

    public String generateText(String prompt) {
        Map<String, Object> body = Map.of(
                "model", MODEL,
                "input", List.of(
                        Map.of(
                                "type", "message",
                                "role", "user",
                                "content", List.of(
                                        Map.of(
                                                "type", "input_text",
                                                "text", prompt
                                        )
                                )
                        )
                )
        );

        log.info("TEXT PROMPT SENT TO OPENAI = {}", prompt);

        Map<?, ?> response = restClient.post()
                .uri("/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        log.info("RAW OPENAI TEXT RESPONSE = {}", response);

        String outputText = extractOutputText(response);
        if (outputText == null || outputText.isBlank()) {
            throw new IllegalStateException("OpenAI text response missing output_text");
        }

        return stripMarkdownCodeFences(outputText);
    }

    private Map<String, Object> buildVisionBody(List<VisionImage> images, String prompt) {
        List<Map<String, Object>> content = new ArrayList<>();
        content.add(Map.of("type", "input_text", "text", prompt));

        for (VisionImage img : images) {
            String mime = (img.mimeType() == null || img.mimeType().isBlank())
                    ? "image/jpeg"
                    : img.mimeType();

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

    private Map<String, Object> buildVisionLogBody(List<VisionImage> images, String prompt) {
        List<Map<String, Object>> content = new ArrayList<>();
        content.add(Map.of("type", "input_text", "text", prompt));

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
        if (outputText instanceof String s && !s.isBlank()) {
            return s;
        }

        Object outputObj = response.get("output");
        if (outputObj instanceof List<?> outputList && !outputList.isEmpty()) {
            Object first = outputList.get(0);
            if (first instanceof Map<?, ?> firstMap) {
                Object contentObj = firstMap.get("content");
                if (contentObj instanceof List<?> contentList && !contentList.isEmpty()) {
                    Object firstContent = contentList.get(0);
                    if (firstContent instanceof Map<?, ?> contentMap) {
                        Object textObj = contentMap.get("text");
                        if (textObj instanceof String s) {
                            return s;
                        }
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
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return defaultValue;
        }

        String v = node.get(field).asText();
        return (v == null || v.isBlank()) ? defaultValue : v;
    }

    private String readNullableText(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }

        String v = node.get(field).asText();
        return (v == null || v.isBlank()) ? null : v;
    }

    private double readDouble(JsonNode node, String field, double defaultValue) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return defaultValue;
        }

        try {
            return node.get(field).asDouble();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private int readInt(JsonNode node, String field, int defaultValue) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return defaultValue;
        }

        try {
            return node.get(field).asInt();
        } catch (Exception e) {
            return defaultValue;
        }
    }
}