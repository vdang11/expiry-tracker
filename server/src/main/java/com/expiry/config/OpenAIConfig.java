package com.expiry.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
public class OpenAIConfig {

    @Bean
    public RestClient openAiRestClient() {

        String apiKey = System.getenv("OPENAI_API_KEY");

        boolean hasKey = apiKey != null && !apiKey.isBlank();

        if (!hasKey) {
            // Do not crash the whole app: non-AI features must keep working.
            // OpenAIClient rejects AI calls with 503 while the key is missing.
            log.warn("OPENAI_API_KEY is not set - AI features (scan, recipes) are disabled");
        }

        String baseUrl = System.getenv("OPENAI_BASE_URL");

        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.openai.com/v1";
        }

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        if (hasKey) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
        }

        return builder.build();
    }
}