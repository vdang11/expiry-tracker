package com.expiry.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Component
@RequiredArgsConstructor
public class HeaderCurrentUserProvider implements CurrentUserProvider {

    private final HttpServletRequest request;

    @Override
    public Long getCurrentUserId() {
        String header = request.getHeader("X-User-Id");

        if (header == null || header.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Id");
        }

        try {
            return Long.parseLong(header);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid X-User-Id");
        }
    }
}