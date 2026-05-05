package com.expiry.security;

import com.expiry.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private static final String CURRENT_USER_ATTR = "currentUserId";

    private final HttpServletRequest request;

    public Long getCurrentUserId() {
        Object userId = request.getAttribute(CURRENT_USER_ATTR);

        if (userId == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        return (Long) userId;
    }
}