package com.expiry.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ExpiryServiceTest {

    private final ExpiryService service = new ExpiryService();

    @Test
    void shouldReturnExpired() {
        LocalDate past = LocalDate.now().minusDays(1);

        ExpiryStatus status = service.calculateStatus(past);

        assertThat(status).isEqualTo(ExpiryStatus.EXPIRED);
    }

    @Test
    void shouldReturnExpiringSoon() {
        LocalDate soon = LocalDate.now().plusDays(2);

        ExpiryStatus status = service.calculateStatus(soon);

        assertThat(status).isEqualTo(ExpiryStatus.EXPIRING_SOON);
    }

    @Test
    void shouldReturnFresh() {
        LocalDate future = LocalDate.now().plusDays(10);

        ExpiryStatus status = service.calculateStatus(future);

        assertThat(status).isEqualTo(ExpiryStatus.FRESH);
    }
}