package com.example.expiry.controller;

import com.example.expiry.dto.ProductResponse;
import com.example.expiry.dto.SaveProductRequest;
import com.example.expiry.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
}