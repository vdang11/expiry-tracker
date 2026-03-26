package com.example.expiry.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class ExpiryService {

    public ExpiryStatus calculateStatus(LocalDate expiryDate) {

        if (expiryDate == null) return ExpiryStatus.FRESH;

        LocalDate today = LocalDate.now();

        if (expiryDate.isBefore(today)) {
            return ExpiryStatus.EXPIRED;
        }

        if (!expiryDate.isAfter(today.plusDays(3))) {
            return ExpiryStatus.EXPIRING_SOON;
        }

        return ExpiryStatus.FRESH;
    }

    public Long calculateDaysLeft(LocalDate expiryDate) {

        if (expiryDate == null) return null;

        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }
}