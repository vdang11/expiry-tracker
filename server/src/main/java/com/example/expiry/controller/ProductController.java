package com.example.expiry.controller;

import com.example.expiry.dto.ProductResponse;
import com.example.expiry.dto.ProductSummaryResponse;
import com.example.expiry.dto.SaveProductRequest;
import com.example.expiry.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse saveConfirmedProduct(@RequestBody SaveProductRequest request) {
        return productService.saveConfirmedProduct(request);
    }

    @GetMapping
    public List<ProductResponse> getProducts(@RequestParam Long userId) {
        return productService.getProductsByUser(userId);
    }

    @GetMapping("/summary")
    public ProductSummaryResponse getSummary(@RequestParam Long userId) {
        return productService.getSummary(userId);
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    // 2. DELETE item
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }

    // 3. CONSUME item
    @PutMapping("/{id}/consume")
    public ProductResponse consume(@PathVariable Long id) {
        return productService.consume(id);
    }

}