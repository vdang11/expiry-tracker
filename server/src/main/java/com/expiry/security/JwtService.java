package com.expiry.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    // ===== 1. CREATE TOKEN =====
    public String generateToken(Long userId, String email) {

        SecretKey key = getSigningKey();

        return Jwts.builder()
                .subject(String.valueOf(userId)) // sub = userId
                .claim("email", email)          // thêm email vào payload
                .issuedAt(new Date())           // thời điểm tạo
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                .signWith(key)                  // ký bằng secret key
                .compact();                    // build thành string JWT
    }

    // ===== 2. EXTRACT USER ID =====
    public Long extractUserId(String token) {
        String subject = extractAllClaims(token).getSubject();
        return Long.parseLong(subject);
    }

    // ===== 3. EXTRACT EMAIL =====
    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    // ===== 4. VALIDATE TOKEN =====
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token); // nếu parse được => hợp lệ
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ===== 5. GET ALL CLAIMS =====
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())   // dùng key để verify chữ ký
                .build()
                .parseSignedClaims(token)      // parse token
                .getPayload();                 // lấy payload
    }

    // ===== 6. BUILD SECRET KEY =====
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret()
                .getBytes(StandardCharsets.UTF_8);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}