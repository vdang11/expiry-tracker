package com.expiry.service;

import com.expiry.dto.ProductResponse;
import com.expiry.dto.ProductSummaryResponse;
import com.expiry.dto.SaveProductRequest;
import com.expiry.entity.Item;
import com.expiry.entity.User;
import com.expiry.repository.ProductRepository;
import com.expiry.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ExpiryDateNormalizer expiryDateNormalizer;
    private final UserRepository userRepository;
    private final ExpiryService expiryService;

    public ProductService(ProductRepository productRepository,
                          ExpiryDateNormalizer expiryDateNormalizer,
                          UserRepository userRepository,
                          ExpiryService expiryService) {
        this.productRepository = productRepository;
        this.expiryDateNormalizer = expiryDateNormalizer;
        this.userRepository = userRepository;
        this.expiryService = expiryService;
    }

    // ================= SAVE =================
    public ProductResponse saveConfirmedProduct(SaveProductRequest request, Long userId) {

        validateRequest(request);

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

        Item savedItem = productRepository.save(item);

        return mapToResponse(savedItem);
    }

    // ================= GET LIST =================
    public List<ProductResponse> getProductsByUser(Long userId) {

        List<Item> items = productRepository.findByUser_IdAndItemStatus(userId, "ACTIVE");

        return items.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ================= SUMMARY =================
    public ProductSummaryResponse getSummary(Long userId) {

        List<Item> items = productRepository.findByUser_IdAndItemStatus(userId, "ACTIVE");

        long expired = 0;
        long expiringSoon = 0;
        long fresh = 0;

        for (Item item : items) {
            ExpiryStatus status = expiryService.calculateStatus(item.getExpiryDate());

            switch (status) {
                case EXPIRED -> expired++;
                case EXPIRING_SOON -> expiringSoon++;
                case FRESH -> fresh++;
            }
        }

        return new ProductSummaryResponse(expired, expiringSoon, fresh);
    }

    // ================= GET BY ID =================
    public ProductResponse getById(Long id, Long userId) {

        Item item = productRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        return mapToResponse(item);
    }

    // ================= DELETE =================
    public void delete(Long id, Long userId) {

        Item item = productRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

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

    // ================= VALIDATION =================
    private void validateRequest(SaveProductRequest request) {

        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required.");
        }

        if (request.getProductName() == null || request.getProductName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item name is required.");
        }

        if (request.getDecisionStatus() == null ||
                !"CONFIRMED".equalsIgnoreCase(request.getDecisionStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only CONFIRMED products can be saved.");
        }

        if (request.getExpiryDate() == null || request.getExpiryDate().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiry date is required.");
        }

        if (request.getDateType() == null || request.getDateType().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date type is required.");
        }
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
                expiryService.calculateStatus(item.getExpiryDate()),
                item.getItemStatus(),
                expiryService.calculateDaysLeft(item.getExpiryDate())
        );
    }
}