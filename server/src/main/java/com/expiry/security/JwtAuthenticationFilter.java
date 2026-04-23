package com.expiry.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String CURRENT_USER_ATTR = "currentUserId";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Không có token -> request public thì đi tiếp, request protected thì Spring Security tự chặn
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token found for path: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);

            if (!jwtService.isTokenValid(token)) {
                log.warn("Invalid JWT token for path: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            Long userId = jwtService.extractUserId(token);

            // 1) Cho code hiện tại của mày dùng qua CurrentUserProvider
            request.setAttribute(CURRENT_USER_ATTR, userId);

            // 2) Báo cho Spring Security biết request này đã authenticated
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.emptyList()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT authenticated successfully. userId={}, path={}", userId, request.getRequestURI());

        } catch (Exception e) {
            log.error("JWT processing failed for path: {}. Reason: {}", request.getRequestURI(), e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}