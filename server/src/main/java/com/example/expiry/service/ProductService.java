package com.example.expiry.service;

import com.example.expiry.dto.ProductResponse;
import com.example.expiry.dto.ProductSummaryResponse;
import com.example.expiry.dto.SaveProductRequest;
import com.example.expiry.model.ExpiryStatus;
import com.example.expiry.model.Item;
import com.example.expiry.model.User;
import com.example.expiry.repository.ProductRepository;
import com.example.expiry.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ExpiryDateNormalizer expiryDateNormalizer;
    private final UserRepository userRepository;
    private final ExpiryService expiryService;

    public ProductService(ProductRepository productRepository,
                          ExpiryDateNormalizer expiryDateNormalizer, UserRepository userRepository, ExpiryService expiryService) {
        this.productRepository = productRepository;
        this.expiryDateNormalizer = expiryDateNormalizer;
        this.userRepository = userRepository;
        this.expiryService = expiryService;
    }

    public ProductResponse saveConfirmedProduct(SaveProductRequest request) {
        validateRequest(request);

        LocalDate normalizedDate = expiryDateNormalizer.normalize(request.getExpiryDate());

        Item item = new Item();
        item.setProductName(request.getProductName().trim());
        item.setExpiryDate(normalizedDate);
        item.setConfidence(request.getConfidence());
        item.setDateType(request.getDateType());
        item.setDecisionStatus(request.getDecisionStatus());
        item.setSuggestedAction(request.getSuggestedAction());
        item.setItemStatus("ACTIVE");

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        item.setUser(user);
        Item savedItem = productRepository.save(item);

        return mapToResponse(savedItem);
    }

    public List<ProductResponse> getProductsByUser(Long userId) {

        List<Item> items = productRepository.findByUser_IdAndItemStatus(userId, "ACTIVE");

        return items.stream()
                .map(this::mapToResponse)
                .toList();
    }

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

    private void validateRequest(SaveProductRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        if (request.getProductName() == null || request.getProductName().isBlank()) {
            throw new IllegalArgumentException("Item name is required.");
        }

        if (request.getDecisionStatus() == null || !"CONFIRMED".equalsIgnoreCase(request.getDecisionStatus())) {
            throw new IllegalArgumentException("Only CONFIRMED products can be saved.");
        }

        if (request.getExpiryDate() == null || request.getExpiryDate().isBlank()) {
            throw new IllegalArgumentException("Expiry date is required.");
        }

        if (request.getDateType() == null || request.getDateType().isBlank()) {
            throw new IllegalArgumentException("Date type is required.");
        }
    }

    public ProductResponse getById(Long id) {

        Item item = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        return mapToResponse(item);
    }

    public void delete(Long id) {

        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Item not found");
        }

        productRepository.deleteById(id);
    }

    // ================= CONSUME =================
    public ProductResponse consume(Long id) {

        Item item = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setItemStatus("CONSUMED");

        Item updated = productRepository.save(item);

        return mapToResponse(updated);
    }

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