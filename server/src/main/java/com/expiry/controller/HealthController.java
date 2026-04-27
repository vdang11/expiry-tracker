package com.expiry.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    // ===== ROOT HEALTH CHECK (Elastic Beanstalk dùng) =====
    @GetMapping("/")
    public String home() {
        return "OK";
    }
}