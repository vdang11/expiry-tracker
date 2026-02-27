package com.example.expiry.controller;

import com.example.expiry.dto.ExpiryResult;
import com.example.expiry.service.ScanExpiryService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/vision")
public class ExpiryController {

    private final ScanExpiryService scanExpiryService;

    public ExpiryController(ScanExpiryService scanExpiryService) {
        this.scanExpiryService = scanExpiryService;
    }

    @PostMapping("/scan")
    public ExpiryResult scan(
            @RequestParam("image") MultipartFile image
    ) {
        return scanExpiryService.scan(image);
    }
}