package com.expiry.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ExpiryDecisionEngineTest {

    private final ExpiryDecisionEngine engine = new ExpiryDecisionEngine();

    private String futureDate(int days) {
        return LocalDate.now().plusDays(days).toString();
    }

    private String pastDate(int days) {
        return LocalDate.now().minusDays(days).toString();
    }

    // ================= CONFIRMED =================

    @Test
    void shouldReturnConfirmedWhenConfidenceHigh() {
        var decision = engine.decide(
                futureDate(10),
                "CONFIRMED",
                "OK",
                0.9
        );

        assertThat(decision.getStatus()).isEqualTo("CONFIRMED");
    }

    // ================= REVIEW =================

    @Test
    void shouldReturnReviewWhenEstimatedDate() {
        var decision = engine.decide(
                futureDate(10),
                "ESTIMATED",
                "OK",
                0.9
        );

        assertThat(decision.getStatus()).isEqualTo("REVIEW");
        assertThat(decision.getReason()).isEqualTo("ESTIMATED_DATE");
    }

    @Test
    void shouldReturnReviewWhenMediumConfidence() {
        var decision = engine.decide(
                futureDate(10),
                "CONFIRMED",
                "OK",
                0.5
        );

        assertThat(decision.getStatus()).isEqualTo("REVIEW");
        assertThat(decision.getReason()).isEqualTo("LOW_CONFIDENCE");
    }

    // ================= REJECT =================

    @Test
    void shouldRejectWhenDateUnknown() {
        var decision = engine.decide(
                "UNKNOWN",
                "CONFIRMED",
                "OK",
                0.9
        );

        assertThat(decision.getStatus()).isEqualTo("REJECTED");
        assertThat(decision.getReason()).isEqualTo("NO_DATE_DETECTED");
    }

    @Test
    void shouldRejectWhenBlurryAndLowConfidence() {
        var decision = engine.decide(
                futureDate(10),
                "CONFIRMED",
                "BLURRY",
                0.5
        );

        assertThat(decision.getStatus()).isEqualTo("REJECTED");
        assertThat(decision.getReason()).isEqualTo("BLURRY_IMAGE");
    }

    @Test
    void shouldRejectWhenInvalidDateFormat() {
        var decision = engine.decide(
                "abc-xyz",
                "CONFIRMED",
                "OK",
                0.9
        );

        assertThat(decision.getStatus()).isEqualTo("REJECTED");
        assertThat(decision.getReason()).isEqualTo("INVALID_DATE_FORMAT");
    }

    @Test
    void shouldRejectWhenPastDate() {
        var decision = engine.decide(
                pastDate(1),
                "CONFIRMED",
                "OK",
                0.9
        );

        assertThat(decision.getStatus()).isEqualTo("REJECTED");
        assertThat(decision.getReason()).isEqualTo("PAST_DATE_DETECTED");
    }

    @Test
    void shouldRejectWhenConfidenceTooLow() {
        var decision = engine.decide(
                futureDate(10),
                "CONFIRMED",
                "OK",
                0.2
        );

        assertThat(decision.getStatus()).isEqualTo("REJECTED");
        assertThat(decision.getReason()).isEqualTo("LOW_CONFIDENCE");
    }

    // ================= EDGE CASE =================

    @Test
    void shouldRejectWhenDateTooFarInPast() {
        var date = LocalDate.now().minusYears(6).toString();

        var decision = engine.decide(
                date,
                "CONFIRMED",
                "OK",
                0.9
        );

        assertThat(decision.getStatus()).isEqualTo("REJECTED");
        assertThat(decision.getReason()).isEqualTo("UNREALISTIC_PAST_DATE");
    }

    @Test
    void shouldRejectWhenDateTooFarInFuture() {
        var date = LocalDate.now().plusYears(6).toString();

        var decision = engine.decide(
                date,
                "CONFIRMED",
                "OK",
                0.9
        );

        assertThat(decision.getStatus()).isEqualTo("REJECTED");
        assertThat(decision.getReason()).isEqualTo("UNREALISTIC_FUTURE_DATE");
    }
}