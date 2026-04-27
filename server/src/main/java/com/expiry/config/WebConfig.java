package com.expiry.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {

        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {

                // ===== READ ENV =====
                String allowedOrigins = System.getenv("CORS_ALLOWED_ORIGINS");

                // ===== FALLBACK FOR DEV =====
                if (allowedOrigins == null || allowedOrigins.isBlank()) {
                    allowedOrigins = "http://localhost:5173";
                }

                registry.addMapping("/**")
                        .allowedOriginPatterns(allowedOrigins.split(","))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false)
                        .maxAge(3600);
            }
        };
    }
}