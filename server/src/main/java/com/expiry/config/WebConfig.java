package com.expiry.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig {

    private static final List<String> DEFAULT_ORIGINS = List.of(
            // ===== LOCAL DEV =====
            "http://localhost:5173",
            "http://localhost:4173",
            "http://127.0.0.1:4173",

            // ===== PROD (CloudFront HTTPS) =====
            "https://d3rhx0vrni0urs.cloudfront.net"
    );

    // Extra origins, comma separated (CORS_ALLOWED_ORIGINS in prod)
    @Value("${cors.allowed-origins:}")
    private String extraOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {

                List<String> origins = new ArrayList<>(DEFAULT_ORIGINS);
                Arrays.stream(extraOrigins.split(","))
                        .map(String::trim)
                        .filter(o -> !o.isEmpty())
                        .forEach(origins::add);

                registry.addMapping("/api/**")
                        .allowedOrigins(origins.toArray(String[]::new))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}