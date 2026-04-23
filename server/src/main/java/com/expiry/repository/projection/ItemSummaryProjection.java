package com.expiry.repository.projection;

public interface ItemSummaryProjection {
    long getExpiredCount();
    long getExpiringSoonCount();
    long getFreshCount();
}