package com.expiry.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "items",
        indexes = {
                @Index(name = "idx_item_user_id", columnList = "user_id"),
                @Index(name = "idx_item_status", columnList = "item_status"),
                @Index(name = "idx_item_expiry_date", columnList = "expiry_date"),
                @Index(name = "idx_item_user_status", columnList = "user_id, item_status")
        }
)
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private double confidence;

    @Column(name = "date_type", nullable = false, length = 20)
    private String dateType;

    @Column(name = "decision_status", nullable = false, length = 20)
    private String decisionStatus;

    @Column(name = "suggested_action", length = 50)
    private String suggestedAction;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "item_status", nullable = false)
    private String itemStatus;

    @Column(name = "last_reminder_sent_date")
    private LocalDate lastReminderSentDate;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.itemStatus == null) {
            this.itemStatus = "ACTIVE";
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}