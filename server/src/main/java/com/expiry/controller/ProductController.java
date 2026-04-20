package com.expiry.controller;

import com.expiry.dto.ProductResponse;
import com.expiry.dto.ProductSummaryResponse;
import com.expiry.dto.SaveProductRequest;
import com.expiry.security.CurrentUserProvider;
import com.expiry.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse saveConfirmedProduct(@RequestBody SaveProductRequest request) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        return productService.saveConfirmedProduct(request, currentUserId);
    }

    @GetMapping
    public Page<ProductResponse> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "expiryDate") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        return productService.getProducts(
                currentUserId,
                page,
                size,
                search,
                filter,
                sortBy,
                direction
        );
    }

    @GetMapping("/summary")
    public ProductSummaryResponse getSummary() {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        return productService.getSummary(currentUserId);
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        return productService.getById(id, currentUserId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        productService.delete(id, currentUserId);
    }

    @PutMapping("/{id}/consume")
    public ProductResponse consume(@PathVariable Long id) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        return productService.consume(id, currentUserId);
    }
}