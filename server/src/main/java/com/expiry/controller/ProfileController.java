package com.expiry.controller;

import com.expiry.entity.User;
import com.expiry.repository.UserRepository;
import com.expiry.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    // ===== GET PROFILE =====
    @GetMapping
    public ProfileResponse getProfile() {

        Long userId = currentUserProvider.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new ProfileResponse(
                user.getEmail(),
                user.isEmailReminderEnabled()
        );
    }

    // ===== TOGGLE EMAIL REMINDER =====
    @PutMapping("/email-reminder")
    public void updateEmailReminder(@RequestBody EmailReminderRequest request) {

        Long userId = currentUserProvider.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmailReminderEnabled(request.enabled());
        userRepository.save(user);
    }

    // ===== DTO =====
    public record ProfileResponse(
            String email,
            boolean emailReminderEnabled
    ) {}

    public record EmailReminderRequest(
            boolean enabled
    ) {}
}