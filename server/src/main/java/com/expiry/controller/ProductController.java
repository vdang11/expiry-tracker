package com.expiry.controller;

import com.expiry.dto.ProductResponse;
import com.expiry.dto.ProductSummaryResponse;
import com.expiry.dto.SaveProductRequest;
import com.expiry.security.CurrentUserProvider; // 🔥 thêm
import com.expiry.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final CurrentUserProvider currentUserProvider; // 🔥 thêm

    public ProductController(ProductService productService,
                             CurrentUserProvider currentUserProvider) { // 🔥 inject thêm
        this.productService = productService;
        this.currentUserProvider = currentUserProvider;
    }

    // 1. SAVE item
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse saveConfirmedProduct(@RequestBody SaveProductRequest request) {
        Long currentUserId = currentUserProvider.getCurrentUserId(); // 🔥 NEW
        return productService.saveConfirmedProduct(request, currentUserId); // 🔥 sửa
    }

    // 2. GET all items của current user
    @GetMapping
    public List<ProductResponse> getProducts() { // ❌ bỏ @RequestParam userId
        Long currentUserId = currentUserProvider.getCurrentUserId(); // 🔥 NEW
        return productService.getProductsByUser(currentUserId); // 🔥 sửa
    }

    // 3. SUMMARY
    @GetMapping("/summary")
    public ProductSummaryResponse getSummary() { // ❌ bỏ userId
        Long currentUserId = currentUserProvider.getCurrentUserId(); // 🔥 NEW
        return productService.getSummary(currentUserId); // 🔥 sửa
    }

    // 4. GET item detail (có ownership check)
    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        Long currentUserId = currentUserProvider.getCurrentUserId(); // 🔥 NEW
        return productService.getById(id, currentUserId); // 🔥 sửa
    }

    // 5. DELETE item
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        Long currentUserId = currentUserProvider.getCurrentUserId(); // 🔥 NEW
        productService.delete(id, currentUserId); // 🔥 sửa
    }

    // 6. CONSUME item
    @PutMapping("/{id}/consume")
    public ProductResponse consume(@PathVariable Long id) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        return productService.consume(id, currentUserId);
    }
}