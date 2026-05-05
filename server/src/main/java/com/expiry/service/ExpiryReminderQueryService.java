package com.expiry.service;

import com.expiry.entity.Item;
import com.expiry.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpiryReminderQueryService {

    private final ItemRepository itemRepository;

    public List<Item> findExpiringSoonItems(int windowDays) {

        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(windowDays);

        return itemRepository.findByItemStatus("ACTIVE")
                .stream()
                .filter(item -> item.getExpiryDate() != null)
                .filter(item ->
                        !item.getExpiryDate().isBefore(today) &&
                                !item.getExpiryDate().isAfter(limit)
                )
                .toList();
    }

    public List<Item> findExpiredItems() {

        LocalDate today = LocalDate.now();

        return itemRepository.findByItemStatus("ACTIVE")
                .stream()
                .filter(item -> item.getExpiryDate() != null)
                .filter(item ->
                        item.getExpiryDate().isBefore(today)
                )
                .toList();
    }
}