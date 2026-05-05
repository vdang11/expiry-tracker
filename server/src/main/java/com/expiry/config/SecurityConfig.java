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
                .cors(cors -> {})

                .authorizeHttpRequests(auth -> auth

                        // ===== HEALTH CHECK (CRITICAL) =====
                        .requestMatchers(
                                "/",                    
                                "/actuator/health"
                        ).permitAll()

                        // ===== AUTH =====
                        .requestMatchers(
                                "/api/users/signup",
                                "/api/users/login"
                        ).permitAll()

                        // nếu cần public scan thì mở
                        // .requestMatchers("/api/vision/scan").permitAll()

                        // ===== PROTECTED =====
                        .anyRequest().authenticated()
                )

                // ===== JWT FILTER =====
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}