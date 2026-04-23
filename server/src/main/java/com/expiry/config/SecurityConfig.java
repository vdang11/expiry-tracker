package com.expiry.config;

import com.expiry.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // ===== PASSWORD =====
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ===== SECURITY FILTER =====
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {}) // dùng WebConfig

                .authorizeHttpRequests(auth -> auth
                        // ===== PUBLIC =====
                        .requestMatchers(
                                "/api/users/signup",
                                "/api/users/login"
                        ).permitAll()

                        // nếu muốn scan public thì mở dòng dưới
                        // .requestMatchers("/api/vision/scan").permitAll()

                        // ===== PROTECTED =====
                        .anyRequest().authenticated()
                )

                // ===== ADD JWT FILTER =====
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}