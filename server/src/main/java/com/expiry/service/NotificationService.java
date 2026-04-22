package com.expiry.service;

import com.expiry.dto.NotificationResponse;
import com.expiry.entity.Notification;
import com.expiry.entity.User;
import com.expiry.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;

    // ===== CREATE =====
    public void createNotification(User user, String title, String message) {
        repo.save(Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .isRead(false)
                .build());
    }

    // ===== GET =====
    public List<NotificationResponse> getUserNotifications(Long userId) {
        return repo.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ===== MARK READ =====
    public void markAsRead(Long id, Long userId) {
        Notification n = repo.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Notification not found"
                ));

        n.setRead(true);
        repo.save(n);
    }

    // ===== DELETE ONE (🔥 FIX CHUẨN) =====
    public void delete(Long id, Long userId) {
        Notification n = repo.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Notification not found"
                ));

        repo.delete(n);
    }

    // ===== DELETE ALL =====
    public void deleteAll(Long userId) {
        List<Notification> list = repo.findByUser_Id(userId);
        repo.deleteAll(list);
    }

    // ===== COUNT =====
    public long countUserUnread(Long userId) {
        return repo.countByUser_IdAndIsReadFalse(userId);
    }

    // ===== MAPPER =====
    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}