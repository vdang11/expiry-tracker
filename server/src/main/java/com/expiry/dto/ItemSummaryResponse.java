package com.expiry.dto;
import lombok.*;
@Getter
@AllArgsConstructor
public class ItemSummaryResponse {
    private long expired;
    private long expiringSoon;
    private long fresh;
}