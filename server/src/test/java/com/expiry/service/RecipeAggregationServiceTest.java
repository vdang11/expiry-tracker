package com.expiry.service;

import com.expiry.dto.RecipeAggregationResult;
import com.expiry.entity.Item;
import com.expiry.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RecipeAggregationServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private RecipeIngredientFilter recipeIngredientFilter;

    @InjectMocks
    private RecipeAggregationService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void should_return_expiring_items_correctly() {
        Long userId = 1L;

        Item milk = buildItem("Milk", LocalDate.now().plusDays(2));
        Item egg = buildItem("Egg", LocalDate.now().plusDays(1));

        when(itemRepository.findByUser_IdAndItemStatus(userId, "ACTIVE"))
                .thenReturn(List.of(milk, egg));

        when(recipeIngredientFilter.isCookableIngredient(any()))
                .thenReturn(true);

        RecipeAggregationResult result = service.getAggregationResult(userId);

        assertThat(result.getExpiringIngredients())
                .containsExactlyInAnyOrder("milk", "egg");
    }

    @Test
    void should_filter_invalid_names() {
        Long userId = 1L;

        Item valid = buildItem("Milk", LocalDate.now().plusDays(2));
        Item blank = buildItem("   ", LocalDate.now().plusDays(2));
        Item nullName = buildItem(null, LocalDate.now().plusDays(2));

        when(itemRepository.findByUser_IdAndItemStatus(userId, "ACTIVE"))
                .thenReturn(List.of(valid, blank, nullName));

        when(recipeIngredientFilter.isCookableIngredient(any()))
                .thenReturn(true);

        RecipeAggregationResult result = service.getAggregationResult(userId);

        assertThat(result.getExpiringIngredients())
                .containsExactly("milk");
    }

    @Test
    void should_limit_stable_items_to_max_3() {
        Long userId = 1L;

        List<Item> items = List.of(
                buildItem("Rice", LocalDate.now().plusDays(5)),
                buildItem("Pasta", LocalDate.now().plusDays(6)),
                buildItem("Bread", LocalDate.now().plusDays(7)),
                buildItem("Cheese", LocalDate.now().plusDays(8))
        );

        when(itemRepository.findByUser_IdAndItemStatus(userId, "ACTIVE"))
                .thenReturn(items);

        when(recipeIngredientFilter.isCookableIngredient(any()))
                .thenReturn(true);

        RecipeAggregationResult result = service.getAggregationResult(userId);

        assertThat(result.getStableIngredients().size()).isEqualTo(3);
    }

    @Test
    void should_merge_without_duplicates() {
        Long userId = 1L;

        Item milk1 = buildItem("Milk", LocalDate.now().plusDays(2));
        Item milk2 = buildItem("Milk", LocalDate.now().plusDays(5));
        Item rice = buildItem("Rice", LocalDate.now().plusDays(5));

        when(itemRepository.findByUser_IdAndItemStatus(userId, "ACTIVE"))
                .thenReturn(List.of(milk1, milk2, rice));

        when(recipeIngredientFilter.isCookableIngredient(any()))
                .thenReturn(true);

        RecipeAggregationResult result = service.getAggregationResult(userId);

        assertThat(result.getIngredients())
                .containsExactly("milk", "rice");
    }

    private Item buildItem(String name, LocalDate expiry) {
        Item item = new Item();
        item.setProductName(name);
        item.setExpiryDate(expiry);
        return item;
    }
}