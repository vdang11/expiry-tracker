package com.expiry.service;

import com.expiry.dto.RecipeAggregationResult;
import com.expiry.entity.Item;
import com.expiry.entity.ItemCategory;
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

        RecipeAggregationResult result = service.getAggregationResult(userId);

        assertThat(result.getIngredients())
                .containsExactly("milk", "rice");
    }

    @Test
    void should_only_use_items_classified_as_ingredient() {
        Long userId = 1L;

        Item egg = buildItem("Egg", LocalDate.now().plusDays(1));
        Item person = buildItem("Nguyen Van Minh", LocalDate.now().plusDays(1), ItemCategory.OTHER);
        Item noodles = buildItem("Mi Hao Hao", LocalDate.now().plusDays(1), ItemCategory.READY_FOOD);
        Item cream = buildItem("Kem chong nang", LocalDate.now().plusDays(1), ItemCategory.COSMETIC);
        Item pending = buildItem("Something", LocalDate.now().plusDays(1), null);

        when(itemRepository.findByUser_IdAndItemStatus(userId, "ACTIVE"))
                .thenReturn(List.of(egg, person, noodles, cream, pending));

        RecipeAggregationResult result = service.getAggregationResult(userId);

        assertThat(result.getExpiringIngredients()).containsExactly("egg");
    }

    @Test
    void should_use_ingredient_name_instead_of_product_name() {
        Long userId = 1L;

        Item egg = buildItem("Trứng gà Ba Huân 10 quả", LocalDate.now().plusDays(1));
        egg.setIngredientName("egg");

        when(itemRepository.findByUser_IdAndItemStatus(userId, "ACTIVE"))
                .thenReturn(List.of(egg));

        RecipeAggregationResult result = service.getAggregationResult(userId);

        assertThat(result.getExpiringIngredients()).containsExactly("egg");
    }

    private Item buildItem(String name, LocalDate expiry) {
        return buildItem(name, expiry, ItemCategory.INGREDIENT);
    }

    private Item buildItem(String name, LocalDate expiry, ItemCategory category) {
        Item item = new Item();
        item.setProductName(name);
        item.setExpiryDate(expiry);
        item.setCategory(category);
        return item;
    }
}