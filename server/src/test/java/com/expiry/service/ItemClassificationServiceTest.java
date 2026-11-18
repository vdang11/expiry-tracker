package com.expiry.service;

import com.expiry.entity.Item;
import com.expiry.entity.ItemCategory;
import com.expiry.repository.ItemRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class ItemClassificationServiceTest {

    private final ItemRepository itemRepository = mock(ItemRepository.class);
    private final ItemClassifier itemClassifier = mock(ItemClassifier.class);
    private final ItemClassificationService service =
            new ItemClassificationService(itemRepository, itemClassifier);

    @Test
    void backfill_classifies_until_nothing_left() {
        Item egg = item("Egg");
        Item minh = item("Nguyen Van Minh");

        when(itemRepository.findTop50ByCategoryIsNullOrderByIdAsc())
                .thenReturn(List.of(egg, minh))
                .thenReturn(List.of());

        when(itemClassifier.classifyAll(anyList())).thenReturn(List.of(
                new ItemClassifier.Classification(ItemCategory.INGREDIENT, "egg"),
                new ItemClassifier.Classification(ItemCategory.OTHER, null)
        ));

        service.backfillAll();

        assertThat(egg.getCategory()).isEqualTo(ItemCategory.INGREDIENT);
        assertThat(egg.getIngredientName()).isEqualTo("egg");
        assertThat(minh.getCategory()).isEqualTo(ItemCategory.OTHER);
        verify(itemRepository).saveAll(List.of(egg, minh));
    }

    @Test
    void backfill_stops_when_ai_cannot_classify_anything() {
        when(itemRepository.findTop50ByCategoryIsNullOrderByIdAsc())
                .thenReturn(List.of(item("Egg")));

        when(itemClassifier.classifyAll(anyList()))
                .thenReturn(Arrays.asList((ItemClassifier.Classification) null));

        service.backfillAll();   // must return instead of looping forever

        verify(itemRepository, times(1)).findTop50ByCategoryIsNullOrderByIdAsc();
        verify(itemRepository, never()).saveAll(anyList());
    }

    @Test
    void classify_pending_splits_into_batches() {
        List<Item> pending = new ArrayList<>();
        for (int i = 0; i < 120; i++) pending.add(item("Item " + i));

        when(itemRepository.findByUser_IdAndCategoryIsNull(1L)).thenReturn(pending);
        when(itemClassifier.classifyAll(anyList())).thenAnswer(inv -> {
            List<?> names = inv.getArgument(0);
            List<ItemClassifier.Classification> out = new ArrayList<>();
            names.forEach(n -> out.add(new ItemClassifier.Classification(ItemCategory.OTHER, null)));
            return out;
        });

        service.classifyPending(1L);

        verify(itemClassifier, times(3)).classifyAll(anyList());   // 50 + 50 + 20
        assertThat(pending).allMatch(i -> i.getCategory() == ItemCategory.OTHER);
    }

    private Item item(String name) {
        Item item = new Item();
        item.setProductName(name);
        return item;
    }
}
