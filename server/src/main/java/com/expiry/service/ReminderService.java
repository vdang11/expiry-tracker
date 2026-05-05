package com.expiry.service;

import com.expiry.config.ReminderProperties;
import com.expiry.entity.Item;
import com.expiry.entity.User;
import com.expiry.repository.ItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.expiry.service.NotificationService;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {

    private final ExpiryReminderQueryService queryService;
    private final ReminderTemplateBuilder templateBuilder;
    private final ReminderEmailService emailService;
    private final ItemRepository itemRepository;
    private final ReminderProperties reminderProperties;
    private final NotificationService notificationService;

    @Transactional
    public int sendDailyReminders() {

        List<Item> expired = queryService.findExpiredItems();
        List<Item> soon = queryService.findExpiringSoonItems(reminderProperties.getWindowDays());

        Map<Long, Map<Long, Item>> itemsByUser = new LinkedHashMap<>();

        add(itemsByUser, expired);
        add(itemsByUser, soon);

        int totalUsersNotified = 0;
        LocalDate today = LocalDate.now();

        for (Map<Long, Item> userItemMap : itemsByUser.values()) {

            List<Item> filteredItems = userItemMap.values().stream()
                    .filter(this::shouldSendReminderForItem)
                    .sorted(Comparator.comparing(Item::getExpiryDate))
                    .toList();
            log.info("USER {} → TOTAL ITEMS = {}",
                    userItemMap.values().iterator().next().getUser().getId(),
                    userItemMap.size()
            );

            log.info("FILTERED ITEMS SIZE = {}", filteredItems.size());
            for (Item item : userItemMap.values()) {
                log.info("ITEM {} → lastReminderSentDate = {}",
                        item.getProductName(),
                        item.getLastReminderSentDate()
                );
            }
            if (filteredItems.isEmpty()) continue;

            User user = filteredItems.get(0).getUser();

            if (user.getEmail() == null || user.getEmail().isBlank()) continue;

            if (!user.isEmailReminderEnabled()) continue;

            String subject = templateBuilder.buildSubject();
            String body = templateBuilder.buildHtmlBody(user.getName(), filteredItems);

            log.info("Sending reminder email to userId={} email={}",
                    user.getId(), user.getEmail());
            log.info("Sending email to userId={} email={}",
                    user.getId(), user.getEmail());

            emailService.sendReminderEmail(user.getEmail(), subject, body);

            notificationService.createNotification(
                    user,
                    "Expiry Reminder",
                    "You have " + filteredItems.size() + " items expiring soon"
            );

            for (Item item : filteredItems) {
                item.setLastReminderSentDate(today);
            }

            itemRepository.saveAll(filteredItems);
            totalUsersNotified++;
        }

        return totalUsersNotified;
    }

    private void add(Map<Long, Map<Long, Item>> itemsByUser, List<Item> items) {
        for (Item item : items) {

            Long userId = item.getUser().getId();

            itemsByUser
                    .computeIfAbsent(userId, k -> new LinkedHashMap<>())
                    .put(item.getId(), item);
        }
    }

    private boolean shouldSendReminderForItem(Item item) {
        return item.getLastReminderSentDate() == null
                || !LocalDate.now().equals(item.getLastReminderSentDate());
    }
}