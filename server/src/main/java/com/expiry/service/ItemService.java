package com.expiry.service;

import com.expiry.dto.*;
import com.expiry.entity.Item;
import com.expiry.entity.User;
import com.expiry.repository.ItemRepository;
import com.expiry.repository.UserRepository;
import com.expiry.repository.projection.ItemSummaryProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private static final String ACTIVE = "ACTIVE";
    private static final String CONSUMED = "CONSUMED";

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ExpiryDateNormalizer expiryDateNormalizer;
    private final ExpiryService expiryService;
    private final NotificationService notificationService;

    // ================= SAVE =================
    @Transactional
    public ItemResponse saveConfirmedProduct(SaveItemRequest request, Long userId) {

        LocalDate normalizedDate = expiryDateNormalizer.normalize(request.getExpiryDate());

        if (normalizedDate == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Expiry date is required"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
                );

        Item item = new Item();
        item.setProductName(request.getProductName().trim());
        item.setExpiryDate(normalizedDate);
        item.setConfidence(request.getConfidence());
        item.setDateType(request.getDateType());
        item.setDecisionStatus(request.getDecisionStatus());
        item.setSuggestedAction(request.getSuggestedAction());
        item.setItemStatus(ACTIVE);
        item.setUser(user);

        Item saved = itemRepository.save(item);

        notificationService.createNotification(
                user,
                "Item Added",
                saved.getProductName() + " has been added"
        );

        return mapToResponse(saved);
    }

    // ================= GET ITEMS =================
    public ItemPageResponse getItems(
            Long userId,
            int page,
            int size,
            String search,
            String filter,
            String sortBy,
            String direction
    ) {

        String keyword = normalizeKeyword(search);
        boolean useContains = keyword != null && keyword.length() >= 3;

        Pageable pageable = PageRequest.of(
                page,
                size,
                buildSort(sortBy, direction)
        );

        LocalDate today = LocalDate.now();
        LocalDate soonDate = today.plusDays(3);

        Page<Item> itemPage = itemRepository.findActiveItemsForList(
                userId,
                keyword,
                useContains,
                normalizeFilter(filter),
                today,
                soonDate,
                pageable
        );

        List<ItemResponse> content = itemPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ItemPageResponse.builder()
                .content(content)
                .page(itemPage.getNumber())
                .size(itemPage.getSize())
                .totalItems(itemPage.getTotalElements())
                .totalPages(itemPage.getTotalPages())
                .build();
    }

    // ================= SUMMARY =================
    public ItemSummaryResponse getSummary(Long userId) {

        LocalDate today = LocalDate.now();
        LocalDate soonDate = today.plusDays(3);

        ItemSummaryProjection summary =
                itemRepository.getItemSummary(userId, today, soonDate);

        return new ItemSummaryResponse(
                summary.getExpiredCount(),
                summary.getExpiringSoonCount(),
                summary.getFreshCount()
        );
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
    @Transactional
    public void delete(Long id, Long userId) {

        Item item = itemRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found")
                );

        User user = item.getUser();
        String productName = item.getProductName();

        itemRepository.delete(item);

        notificationService.createNotification(
                user,
                "Item Deleted",
                productName + " has been deleted"
        );
    }

    // ================= CONSUME =================
    @Transactional
    public ItemResponse consume(Long id, Long userId) {

        Item item = itemRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found")
                );

        item.setItemStatus(CONSUMED);

        Item updated = itemRepository.save(item);

        notificationService.createNotification(
                updated.getUser(),
                "Item Consumed",
                updated.getProductName() + " has been consumed"
        );

        return mapToResponse(updated);
    }

    // ================= HELPERS =================
    private String normalizeKeyword(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return search.trim();
    }

    private String normalizeFilter(String filter) {
        if (filter == null || filter.isBlank()) {
            return "all";
        }

        return switch (filter.toLowerCase()) {
            case "expired" -> "expired";
            case "soon" -> "soon";
            case "ok" -> "ok";
            default -> "all";
        };
    }

    private Sort buildSort(String sortBy, String direction) {

        String field = mapSortField(sortBy);

        Sort.Direction dir =
                "desc".equalsIgnoreCase(direction)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        return Sort.by(dir, field);
    }

    private String mapSortField(String sortBy) {
        if ("productName".equalsIgnoreCase(sortBy)) {
            return "productName";
        }
        return "expiryDate";
    }

    // ================= MAPPER =================
    private ItemResponse mapToResponse(Item item) {

        ExpiryStatus expiryStatus =
                expiryService.calculateStatus(item.getExpiryDate());

        Long daysLeft =
                expiryService.calculateDaysLeft(item.getExpiryDate());

        return new ItemResponse(
                item.getId(),
                item.getProductName(),
                item.getExpiryDate().toString(),
                item.getConfidence(),
                item.getDateType(),
                item.getDecisionStatus(),
                item.getSuggestedAction(),
                expiryStatus,
                item.getItemStatus(),
                daysLeft
        );
    }
}