package com.expiry.service;

import com.expiry.dto.ItemResponse;
import com.expiry.dto.ItemSummaryResponse;
import com.expiry.dto.SaveItemRequest;
import com.expiry.entity.Item;
import com.expiry.entity.User;
import com.expiry.repository.ItemRepository;
import com.expiry.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ExpiryDateNormalizer expiryDateNormalizer;
    private final ExpiryService expiryService;
    private final NotificationService notificationService;

    // ================= SAVE =================
    public ItemResponse saveConfirmedProduct(SaveItemRequest request, Long userId) {

        LocalDate normalizedDate = expiryDateNormalizer.normalize(request.getExpiryDate());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Item item = new Item();
        item.setProductName(request.getProductName().trim());
        item.setExpiryDate(normalizedDate);
        item.setConfidence(request.getConfidence());
        item.setDateType(request.getDateType());
        item.setDecisionStatus(request.getDecisionStatus());
        item.setSuggestedAction(request.getSuggestedAction());
        item.setItemStatus("ACTIVE");
        item.setUser(user);

        Item saved = itemRepository.save(item);

        //NOTIFICATION (ADD)
        notificationService.createNotification(
                user,
                "Item Added",
                saved.getProductName() + " has been added"
        );

        return mapToResponse(saved);
    }

    // ================= GET ITEMS =================
    public Page<ItemResponse> getItems(
            Long userId,
            int page,
            int size,
            String search,
            String filter,
            String sortBy,
            String direction
    ) {

        String keyword = (search == null || search.isBlank()) ? null : search.trim();
        boolean useContains = keyword != null && keyword.length() >= 3;

        List<Item> items;

        if (keyword != null) {
            if (useContains) {
                items = itemRepository
                        .findByUser_IdAndItemStatusAndProductNameContainingIgnoreCase(
                                userId, "ACTIVE", keyword
                        );
            } else {
                items = itemRepository
                        .findByUser_IdAndItemStatusAndProductNameStartingWithIgnoreCase(
                                userId, "ACTIVE", keyword
                        );
            }
        } else {
            items = itemRepository.findByUser_IdAndItemStatus(userId, "ACTIVE");
        }

        List<ItemResponse> filtered = items.stream()
                .filter(item -> matchFilter(item, filter))
                .map(this::mapToResponse)
                .sorted(buildComparator(sortBy, direction))
                .toList();

        return toPage(filtered, page, size);
    }

    // ================= FILTER =================
    private boolean matchFilter(Item item, String filter) {

        if (item.getExpiryDate() == null) return false;

        ExpiryStatus status = expiryService.calculateStatus(item.getExpiryDate());

        if (filter == null || filter.equals("all")) return true;

        return switch (filter) {
            case "expired" -> status == ExpiryStatus.EXPIRED;
            case "soon" -> status == ExpiryStatus.EXPIRING_SOON;
            case "ok" -> status == ExpiryStatus.FRESH;
            default -> true;
        };
    }

    // ================= SORT =================
    private Comparator<ItemResponse> buildComparator(String sortBy, String direction) {

        boolean desc = "desc".equalsIgnoreCase(direction);

        Comparator<ItemResponse> comparator;

        switch (sortBy) {
            case "productName" ->
                    comparator = Comparator.comparing(ItemResponse::getProductName, String.CASE_INSENSITIVE_ORDER);
            case "expiryDate" ->
                    comparator = Comparator.comparing(ItemResponse::getExpiryDate);
            default ->
                    comparator = Comparator.comparing(ItemResponse::getExpiryDate);
        }

        return desc ? comparator.reversed() : comparator;
    }

    // ================= PAGINATION =================
    private Page<ItemResponse> toPage(List<ItemResponse> list, int page, int size) {

        int start = page * size;

        if (start >= list.size()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), list.size());
        }

        int end = Math.min(start + size, list.size());

        return new PageImpl<>(
                list.subList(start, end),
                PageRequest.of(page, size),
                list.size()
        );
    }

    // ================= SUMMARY =================
    public ItemSummaryResponse getSummary(Long userId) {

        List<Item> items = itemRepository.findByUser_IdAndItemStatus(userId, "ACTIVE");

        long expired = 0;
        long expiringSoon = 0;
        long fresh = 0;

        for (Item item : items) {

            if (item.getExpiryDate() == null) continue;

            ExpiryStatus status = expiryService.calculateStatus(item.getExpiryDate());

            switch (status) {
                case EXPIRED -> expired++;
                case EXPIRING_SOON -> expiringSoon++;
                case FRESH -> fresh++;
            }
        }

        return new ItemSummaryResponse(expired, expiringSoon, fresh);
    }

    // ================= DETAIL =================
    public ItemResponse getById(Long id, Long userId) {

        Item item = itemRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found")
                );

        return mapToResponse(item);
    }

    // ================= DELETE =================
    public void delete(Long id, Long userId) {

        Item item = itemRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found")
                );

        itemRepository.delete(item);

        // NOTIFICATION (DELETE)
        notificationService.createNotification(
                item.getUser(),
                "Item Deleted",
                item.getProductName() + " has been deleted"
        );
    }

    // ================= CONSUME =================
    public ItemResponse consume(Long id, Long userId) {

        Item item = itemRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found")
                );

        item.setItemStatus("CONSUMED");

        Item updated = itemRepository.save(item);

        //NOTIFICATION (CONSUME)
        notificationService.createNotification(
                item.getUser(),
                "Item Consumed",
                item.getProductName() + " has been consumed"
        );

        return mapToResponse(updated);
    }

    // ================= MAPPER =================
    private ItemResponse mapToResponse(Item item) {

        return new ItemResponse(
                item.getId(),
                item.getProductName(),
                item.getExpiryDate() != null ? item.getExpiryDate().toString() : null,
                item.getConfidence(),
                item.getDateType(),
                item.getDecisionStatus(),
                item.getSuggestedAction(),
                item.getExpiryDate() != null
                        ? expiryService.calculateStatus(item.getExpiryDate())
                        : null,
                item.getItemStatus(),
                item.getExpiryDate() != null
                        ? expiryService.calculateDaysLeft(item.getExpiryDate())
                        : null
        );
    }
}