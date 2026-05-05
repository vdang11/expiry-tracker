package com.expiry.controller;

import com.expiry.dto.NotificationResponse;
import com.expiry.security.CurrentUserProvider;
import com.expiry.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;
    private final CurrentUserProvider currentUserProvider;

    // ===== GET =====
    @GetMapping
    public List<NotificationResponse> getAll() {
        return service.getUserNotifications(
                currentUserProvider.getCurrentUserId()
        );
    }

    // ===== READ =====
    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        service.markAsRead(
                id,
                currentUserProvider.getCurrentUserId()
        );
    }

    // ===== DELETE ONE =====
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(
                id,
                currentUserProvider.getCurrentUserId()
        );
    }

    // ===== DELETE ALL =====
    @DeleteMapping
    public void deleteAll() {
        service.deleteAll(
                currentUserProvider.getCurrentUserId()
        );
    }
}