package com.expiry.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPageResponse {

    private List<ItemResponse> content;

    private int page;
    private int size;

    private long totalItems;
    private int totalPages;
}