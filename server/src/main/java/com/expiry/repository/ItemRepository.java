package com.expiry.repository;

import com.expiry.entity.Item;
import com.expiry.repository.projection.ItemSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    // ===== PAGE + SEARCH + FILTER =====
    @Query("""
            SELECT i
            FROM Item i
            WHERE i.user.id = :userId
              AND i.itemStatus = 'ACTIVE'
              AND (
                    :keyword IS NULL
                    OR (
                        :useContains = true
                        AND LOWER(i.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    )
                    OR (
                        :useContains = false
                        AND LOWER(i.productName) LIKE LOWER(CONCAT(:keyword, '%'))
                    )
                  )
              AND (
                    :filter = 'all'
                    OR (:filter = 'expired' AND i.expiryDate < :today)
                    OR (:filter = 'soon' AND i.expiryDate >= :today AND i.expiryDate <= :soonDate)
                    OR (:filter = 'ok' AND i.expiryDate > :soonDate)
                  )
            """)
    Page<Item> findActiveItemsForList(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("useContains") boolean useContains,
            @Param("filter") String filter,
            @Param("today") LocalDate today,
            @Param("soonDate") LocalDate soonDate,
            Pageable pageable
    );

    // ===== SUMMARY =====
    @Query("""
            SELECT
                COUNT(CASE WHEN i.expiryDate < :today THEN 1 END) AS expiredCount,
                COUNT(CASE WHEN i.expiryDate >= :today AND i.expiryDate <= :soonDate THEN 1 END) AS expiringSoonCount,
                COUNT(CASE WHEN i.expiryDate > :soonDate THEN 1 END) AS freshCount
            FROM Item i
            WHERE i.user.id = :userId
              AND i.itemStatus = 'ACTIVE'
            """)
    ItemSummaryProjection getItemSummary(
            @Param("userId") Long userId,
            @Param("today") LocalDate today,
            @Param("soonDate") LocalDate soonDate
    );

    // ===== ALL ACTIVE FOR REMINDER JOB =====
    @Query("""
            SELECT i
            FROM Item i
            WHERE i.itemStatus = :itemStatus
            """)
    java.util.List<Item> findByItemStatus(String itemStatus);

    @Query("""
            SELECT i
            FROM Item i
            WHERE i.itemStatus = :itemStatus
              AND i.expiryDate IS NOT NULL
            """)
    java.util.List<Item> findByItemStatusAndExpiryDateIsNotNull(String itemStatus);

    // ===== DETAIL =====
    Optional<Item> findByIdAndUser_Id(Long id, Long userId);

    // ===== FOR AGGREGATION =====
    List<Item> findByUser_IdAndItemStatus(Long userId, String itemStatus);
}

