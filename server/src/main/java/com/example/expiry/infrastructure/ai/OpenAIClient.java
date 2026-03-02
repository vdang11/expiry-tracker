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

                Return JSON ONLY (no markdown, no explanations) with exactly this format:
                {
                  "expiryDate": "YYYY-MM-DD or UNKNOWN",
                  "confidence": 0.0,
                  "productName": "string or null",
                  "productNameConfidence": 0.0
                }

                Rules:
                - expiryDate: prefer YYYY-MM-DD. If not sure, use UNKNOWN.
                - confidence and productNameConfidence: number between 0.0 and 1.0
                - productName: a short food product name only (no weight, no nutrition, no expiry text). If not found, use null.
                """;

        // =========================
        // 2) BODY GỬI (CÓ BASE64)
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
            double confidence = readDouble(node, "confidence", 0.0);

            String rawProductName = readNullableText(node, "productName");
            double productNameConfidence = readDouble(node, "productNameConfidence", 0.0);

            String productName = ProductNameNormalizer.normalize(rawProductName);

            return new ExpiryResult(expiryDate, confidence, productName, productNameConfidence);

        } catch (Exception e) {
            System.out.println(">>> OPENAI CALL FAILED");
            e.printStackTrace();
            throw new RuntimeException("Scan vision failed", e);
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
}
