package com.expiry.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class ExpiryDateNormalizer {

    public LocalDate normalize(String rawExpiryDate) {
        if (rawExpiryDate == null || rawExpiryDate.isBlank()) {
            throw new IllegalArgumentException("Expiry date is required.");
        }

        String value = rawExpiryDate.trim();

        if ("UNKNOWN".equalsIgnoreCase(value)) {
            throw new IllegalArgumentException("Expiry date is UNKNOWN and cannot be saved.");
        }

        // yyyy-MM-dd
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ignored) {
        }

        // yyyy-MM -> last day of month
        try {
            YearMonth yearMonth = YearMonth.parse(value);
            return yearMonth.atEndOfMonth();
        } catch (DateTimeParseException ignored) {
        }

        // MM/yyyy -> convert then last day of month
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
            YearMonth yearMonth = YearMonth.parse(value, formatter);
            return yearMonth.atEndOfMonth();
        } catch (DateTimeParseException ignored) {
        }

        // yyyy -> reject
        if (value.matches("^\\d{4}$")) {
            throw new IllegalArgumentException("Year-only expiry date is too vague to save.");
        }

        throw new IllegalArgumentException("Unsupported expiry date format: " + value);
    }
}