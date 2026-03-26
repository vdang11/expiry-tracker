package com.example.expiry.dto;
import lombok.*;
@Getter
@AllArgsConstructor
public class ProductSummaryResponse {
    private long expired;
    private long expiringSoon;
    private long fresh;
}