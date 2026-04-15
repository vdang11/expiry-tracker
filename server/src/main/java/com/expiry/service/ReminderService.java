package com.expiry.service;

import com.expiry.config.ReminderProperties;
import com.expiry.entity.Item;
import com.expiry.entity.User;
import com.expiry.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ExpiryReminderQueryService queryService;
    private final ReminderTemplateBuilder templateBuilder;
    private final ReminderEmailService emailService;
    private final ReminderProperties reminderProperties;
    private final ProductRepository productRepository;

    public int sendDailyReminders() {
        List<Item> expiredItems = queryService.findExpiredItems();
        List<Item> expiringSoonItems = queryService.findExpiringSoonItems(reminderProperties.getWindowDays());

        Map<Long, List<Item>> itemsByUser = new LinkedHashMap<>();

        addItemsGroupedByUser(itemsByUser, expiredItems);
        addItemsGroupedByUser(itemsByUser, expiringSoonItems);

        int totalUsersNotified = 0;

        for (List<Item> userItems : itemsByUser.values()) {
            List<Item> filteredItems = userItems.stream()
                    .filter(this::shouldSendReminderForItem)
                    .toList();

            if (filteredItems.isEmpty()) {
                continue;
            }

            User user = filteredItems.get(0).getUser();

            if (user.getEmail() == null || user.getEmail().isBlank()) {
                continue;
            }

            String subject = templateBuilder.buildSubject();
            String body = templateBuilder.buildHtmlBody(user.getName(), filteredItems);

            emailService.sendReminderEmail(user.getEmail(), subject, body);

            LocalDate today = LocalDate.now();
            for (Item item : filteredItems) {
                item.setLastReminderSentDate(today);
            }
            productRepository.saveAll(filteredItems);

            totalUsersNotified++;
        }

        return totalUsersNotified;
    }

    private void addItemsGroupedByUser(Map<Long, List<Item>> itemsByUser, List<Item> items) {
        Map<Long, List<Item>> grouped = items.stream()
                .filter(item -> item.getUser() != null && item.getUser().getId() != null)
                .collect(Collectors.groupingBy(item -> item.getUser().getId(), LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<Long, List<Item>> entry : grouped.entrySet()) {
            itemsByUser.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                    .addAll(entry.getValue());
        }
    }

    private boolean shouldSendReminderForItem(Item item) {
        return item.getLastReminderSentDate() == null
                || !LocalDate.now().equals(item.getLastReminderSentDate());
    }
}