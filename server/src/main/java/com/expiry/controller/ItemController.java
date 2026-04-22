package com.expiry.controller;

import com.expiry.dto.ItemResponse;
import com.expiry.dto.ItemSummaryResponse;
import com.expiry.dto.SaveItemRequest;
import com.expiry.security.CurrentUserProvider;
import com.expiry.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse saveConfirmedProduct(@RequestBody SaveItemRequest request) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        return itemService.saveConfirmedProduct(request, currentUserId);
    }

    @GetMapping
    public Page<ItemResponse> getItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "expiryDate") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        return itemService.getItems(
                currentUserId,
                page,
                size,
                search,
                filter,
                sortBy,
                direction
        );
    }

    @GetMapping("/summary")
    public ItemSummaryResponse getSummary() {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        return itemService.getSummary(currentUserId);
    }

    @GetMapping("/{id}")
    public ItemResponse getById(@PathVariable Long id) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        return itemService.getById(id, currentUserId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        itemService.delete(id, currentUserId);
    }

    @PutMapping("/{id}/consume")
    public ItemResponse consume(@PathVariable Long id) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        return itemService.consume(id, currentUserId);
    }
}