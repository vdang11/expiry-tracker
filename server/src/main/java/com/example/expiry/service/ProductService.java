package com.example.expiry.service;

import com.example.expiry.dto.ProductResponse;
import com.example.expiry.dto.SaveProductRequest;
import com.example.expiry.entity.Item;
import com.example.expiry.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ExpiryDateNormalizer expiryDateNormalizer;

    public ProductService(ProductRepository productRepository,
                          ExpiryDateNormalizer expiryDateNormalizer) {
        this.productRepository = productRepository;
        this.expiryDateNormalizer = expiryDateNormalizer;
    }

    public ProductResponse saveConfirmedProduct(SaveProductRequest request) {
        validateRequest(request);

        LocalDate normalizedDate = expiryDateNormalizer.normalize(request.getExpiryDate());

        Item item = new Item();
        item.setProductName(request.getProductName().trim());
        item.setExpiryDate(normalizedDate);
        item.setConfidence(request.getConfidence());
        item.setDateType(request.getDateType());
        item.setDecisionStatus(request.getStatus());
        item.setSuggestedAction(request.getSuggestedAction());

        Item savedItem = productRepository.save(item);

        return new ProductResponse(
                savedItem.getId(),
                savedItem.getProductName(),
                savedItem.getExpiryDate().toString(),
                savedItem.getConfidence(),
                savedItem.getDateType(),
                savedItem.getDecisionStatus(),
                savedItem.getSuggestedAction()
        );
    }

    private void validateRequest(SaveProductRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        if (request.getProductName() == null || request.getProductName().isBlank()) {
            throw new IllegalArgumentException("Item name is required.");
        }

        if (request.getStatus() == null || !"CONFIRMED".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalArgumentException("Only CONFIRMED products can be saved.");
        }

        if (request.getExpiryDate() == null || request.getExpiryDate().isBlank()) {
            throw new IllegalArgumentException("Expiry date is required.");
        }

        if (request.getDateType() == null || request.getDateType().isBlank()) {
            throw new IllegalArgumentException("Date type is required.");
        }
    }
}