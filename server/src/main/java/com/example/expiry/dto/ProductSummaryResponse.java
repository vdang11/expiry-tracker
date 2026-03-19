package com.example.expiry.dto;

public class ProductSummaryResponse {

    private long expired;
    private long expiringSoon;
    private long fresh;

    public ProductSummaryResponse(long expired, long expiringSoon, long fresh) {
        this.expired = expired;
        this.expiringSoon = expiringSoon;
        this.fresh = fresh;
    }

    public long getExpired() { return expired; }
    public long getExpiringSoon() { return expiringSoon; }
    public long getFresh() { return fresh; }
}