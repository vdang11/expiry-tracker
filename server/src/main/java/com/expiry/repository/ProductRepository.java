package com.expiry.repository;

import com.expiry.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Item, Long> {

    // ===== ALL =====
    List<Item> findByUser_IdAndItemStatus(Long userId, String itemStatus);

    // ===== PREFIX =====
    List<Item> findByUser_IdAndItemStatusAndProductNameStartingWithIgnoreCase(
            Long userId,
            String itemStatus,
            String keyword
    );

    // ===== CONTAINS =====
    List<Item> findByUser_IdAndItemStatusAndProductNameContainingIgnoreCase(
            Long userId,
            String itemStatus,
            String keyword
    );

    // ===== DETAIL =====
    Optional<Item> findByIdAndUser_Id(Long id, Long userId);
}