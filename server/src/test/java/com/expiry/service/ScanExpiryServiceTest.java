package com.expiry.service;

import com.expiry.dto.ExpiryResult;
import com.expiry.infrastructure.ai.OpenAIClient;
import com.expiry.infrastructure.image.ImageOptimizer;
import com.expiry.infrastructure.image.ImageValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ScanExpiryServiceTest {

    @Mock
    private OpenAIClient openAIClient;

    @Mock
    private ExpiryDecisionEngine decisionEngine;

    @Mock
    private ImageValidator imageValidator;

    @Mock
    private ImageOptimizer imageOptimizer;

    @Mock
    private ExpiryPromptBuilder promptBuilder;

    @InjectMocks
    private ScanExpiryService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // =========================
    // TEST 1: happy path
    // =========================
    @Test
    void should_process_successfully() throws Exception {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.jpg",
                        "image/jpeg",
                        "abc".getBytes()
                );

        ImageOptimizer.OptimizedImage optimized =
                new ImageOptimizer.OptimizedImage(
                        "abc".getBytes(),
                        "image/jpeg"
                );

        when(imageOptimizer.optimize(any(), any()))
                .thenReturn(optimized);

        when(promptBuilder.buildPrompt())
                .thenReturn("prompt");

        ExpiryResult aiResult = new ExpiryResult();
        aiResult.setExpiryDate("2026-05-01");
        aiResult.setDateType("CONFIRMED");
        aiResult.setImageQuality("GOOD");
        aiResult.setExpiryConfidence(0.9);
        aiResult.setProductName("Milk");
        aiResult.setProductNameConfidence(0.9);

        when(openAIClient.callVision(any(), any()))
                .thenReturn(aiResult);

        ExpiryDecisionEngine.Decision decision =
                new ExpiryDecisionEngine.Decision(
                        "CONFIRMED",
                        "OK",
                        "KEEP"
                );

        when(decisionEngine.decide(
                anyString(),    // expiryDate
                anyString(),    // dateType
                anyString(),    // imageQuality
                anyDouble()     // confidence (primitive double)
        )).thenReturn(decision);

        ExpiryResult result = service.scan(List.of(file));

        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        assertThat(result.getExpiryDate()).isEqualTo("2026-05-01");
    }

    // =========================
    // TEST 2: reject flow
    // =========================
    @Test
    void should_set_unknown_when_rejected() throws Exception {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.jpg",
                        "image/jpeg",
                        "abc".getBytes()
                );

        ImageOptimizer.OptimizedImage optimized =
                new ImageOptimizer.OptimizedImage(
                        "abc".getBytes(),
                        "image/jpeg"
                );

        when(imageOptimizer.optimize(any(), any()))
                .thenReturn(optimized);

        when(promptBuilder.buildPrompt())
                .thenReturn("prompt");

        ExpiryResult aiResult = new ExpiryResult();
        aiResult.setExpiryDate("2026-05-01");
        aiResult.setDateType("CONFIRMED");
        aiResult.setImageQuality("GOOD");
        aiResult.setExpiryConfidence(0.2);

        when(openAIClient.callVision(any(), any()))
                .thenReturn(aiResult);

        ExpiryDecisionEngine.Decision decision =
                new ExpiryDecisionEngine.Decision(
                        "REJECTED",
                        "LOW_CONFIDENCE",
                        "RESCAN"
                );

        when(decisionEngine.decide(anyString(), anyString(), anyString(), anyDouble()))
                .thenReturn(decision);

        ExpiryResult result = service.scan(List.of(file));

        assertThat(result.getStatus()).isEqualTo("REJECTED");
        assertThat(result.getExpiryDate()).isEqualTo("UNKNOWN");
    }

    // =========================
    // TEST 3: fallback
    // =========================
    @Test
    void should_fallback_when_error() throws Exception {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.jpg",
                        "image/jpeg",
                        "abc".getBytes()
                );

        when(imageOptimizer.optimize(any(), any()))
                .thenThrow(new RuntimeException("fail"));

        ExpiryResult result = service.scan(List.of(file));

        assertThat(result.getStatus()).isEqualTo("REJECTED");
        assertThat(result.getExpiryDate()).isEqualTo("UNKNOWN");
        assertThat(result.getSuggestedAction()).isEqualTo("ASK_USER_RESCAN");
    }
}