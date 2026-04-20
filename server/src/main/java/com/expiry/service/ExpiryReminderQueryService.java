package com.expiry.service;

import com.expiry.entity.Item;
import com.expiry.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpiryReminderQueryService {

    private final ProductRepository productRepository;
    private final ExpiryService expiryService;

    public List<Item> findExpiringSoonItems(int windowDays) {

        List<Item> items = productRepository.findByUser_IdAndItemStatus(null, "ACTIVE");

        return items.stream()
                .filter(item -> item.getExpiryDate() != null)
                .filter(item ->
                        expiryService.calculateStatus(item.getExpiryDate())
                                == ExpiryStatus.EXPIRING_SOON
                )
                .toList();
    }

    public List<Item> findExpiredItems() {

        List<Item> items = productRepository.findByUser_IdAndItemStatus(null, "ACTIVE");

        return items.stream()
                .filter(item -> item.getExpiryDate() != null)
                .filter(item ->
                        expiryService.calculateStatus(item.getExpiryDate())
                                == ExpiryStatus.EXPIRED
                )
                .toList();
    }
}