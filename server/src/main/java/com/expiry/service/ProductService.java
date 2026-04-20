package com.expiry.service;

import com.expiry.dto.ProductResponse;
import com.expiry.dto.ProductSummaryResponse;
import com.expiry.dto.SaveProductRequest;
import com.expiry.entity.Item;
import com.expiry.entity.User;
import com.expiry.repository.ProductRepository;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ExpiryDateNormalizer expiryDateNormalizer;
    private final ExpiryService expiryService;

    // ================= SAVE =================
    public ProductResponse saveConfirmedProduct(SaveProductRequest request, Long userId) {

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

        Item saved = productRepository.save(item);

        return mapToResponse(saved);
    }

    // ================= GET PRODUCTS (CORE) =================
    public Page<ProductResponse> getProducts(
            Long userId,
            int page,
            int size,
            String search,
            String filter,
            String sortBy,
            String direction
    ) {

        String keyword = (search == null || search.isBlank()) ? null : search.trim();

        // 🔥 HYBRID SEARCH RULE
        boolean useContains = keyword != null && keyword.length() >= 3;

        List<Item> items;

        // ===== FETCH =====
        if (keyword != null) {
            if (useContains) {
                items = productRepository
                        .findByUser_IdAndItemStatusAndProductNameContainingIgnoreCase(
                                userId,
                                "ACTIVE",
                                keyword
                        );
            } else {
                items = productRepository
                        .findByUser_IdAndItemStatusAndProductNameStartingWithIgnoreCase(
                                userId,
                                "ACTIVE",
                                keyword
                        );
            }
        } else {
            items = productRepository.findByUser_IdAndItemStatus(userId, "ACTIVE");
        }

        // ===== FILTER (DÙNG ExpiryService) =====
        List<ProductResponse> filtered = items.stream()
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
    private Comparator<ProductResponse> buildComparator(String sortBy, String direction) {

        boolean desc = "desc".equalsIgnoreCase(direction);

        Comparator<ProductResponse> comparator;

        switch (sortBy) {
            case "productName" ->
                    comparator = Comparator.comparing(ProductResponse::getProductName, String.CASE_INSENSITIVE_ORDER);
            case "expiryDate" ->
                    comparator = Comparator.comparing(ProductResponse::getExpiryDate);
            default ->
                    comparator = Comparator.comparing(ProductResponse::getExpiryDate);
        }

        return desc ? comparator.reversed() : comparator;
    }

    // ================= PAGINATION =================
    private Page<ProductResponse> toPage(List<ProductResponse> list, int page, int size) {

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
    public ProductSummaryResponse getSummary(Long userId) {

        List<Item> items = productRepository.findByUser_IdAndItemStatus(userId, "ACTIVE");

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

        return new ProductSummaryResponse(expired, expiringSoon, fresh);
    }

    // ================= DETAIL =================
    public ProductResponse getById(Long id, Long userId) {

        Item item = productRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found")
                );

        return mapToResponse(item);
    }

    // ================= DELETE =================
    public void delete(Long id, Long userId) {

        Item item = productRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found")
                );

        productRepository.delete(item);
    }

    // ================= CONSUME =================
    public ProductResponse consume(Long id, Long userId) {

        Item item = productRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found")
                );

        item.setItemStatus("CONSUMED");

        Item updated = productRepository.save(item);

        return mapToResponse(updated);
    }

    // ================= MAPPER =================
    private ProductResponse mapToResponse(Item item) {

        return new ProductResponse(
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