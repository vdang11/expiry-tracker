package com.expiry.service;

import com.expiry.entity.Item;
import com.expiry.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpiryReminderQueryService {

    private final ProductRepository productRepository;

    public List<Item> findExpiringSoonItems(int windowDays) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(windowDays);

        return productRepository.findByExpiryDateBetweenAndItemStatus(
                today,
                endDate,
                "ACTIVE"
        );
    }

    public List<Item> findExpiredItems() {
        return productRepository.findByExpiryDateBeforeAndItemStatus(
                LocalDate.now(),
                "ACTIVE"
        );
    }
}