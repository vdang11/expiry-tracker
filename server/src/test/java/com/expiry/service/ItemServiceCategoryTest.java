package com.expiry.service;

import com.expiry.dto.SaveItemRequest;
import com.expiry.entity.Item;
import com.expiry.entity.ItemCategory;
import com.expiry.entity.User;
import com.expiry.repository.ItemRepository;
import com.expiry.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ItemServiceCategoryTest {

    private final ItemRepository itemRepository = mock(ItemRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ExpiryDateNormalizer normalizer = mock(ExpiryDateNormalizer.class);
    private final ItemClassifier classifier = mock(ItemClassifier.class);

    private final ItemService service = new ItemService(
            itemRepository, userRepository, normalizer,
            mock(ExpiryService.class), mock(NotificationService.class), classifier
    );

    @BeforeEach
    void setup() {
        when(normalizer.normalize(any())).thenReturn(LocalDate.now().plusDays(3));
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void ai_decides_when_user_did_not_choose() {
        when(classifier.classify("Trứng gà"))
                .thenReturn(new ItemClassifier.Classification(ItemCategory.INGREDIENT, "egg"));

        Item saved = save("Trứng gà", null);

        assertThat(saved.getCategory()).isEqualTo(ItemCategory.INGREDIENT);
        assertThat(saved.getIngredientName()).isEqualTo("egg");
    }

    @Test
    void user_choice_other_than_ingredient_skips_ai() {
        Item saved = save("Panadol", ItemCategory.MEDICINE);

        assertThat(saved.getCategory()).isEqualTo(ItemCategory.MEDICINE);
        verify(classifier, never()).classify(anyString());
    }

    @Test
    void user_choice_ingredient_wins_over_ai_and_keeps_english_name() {
        when(classifier.classify("Rau ngót"))
                .thenReturn(new ItemClassifier.Classification(ItemCategory.OTHER, null));

        Item saved = save("Rau ngót", ItemCategory.INGREDIENT);

        assertThat(saved.getCategory()).isEqualTo(ItemCategory.INGREDIENT);
        assertThat(saved.getIngredientName()).isEqualTo("rau ngót");
    }

    @Test
    void user_choice_ingredient_uses_ai_english_name_when_available() {
        when(classifier.classify("Thịt ba chỉ"))
                .thenReturn(new ItemClassifier.Classification(ItemCategory.INGREDIENT, "pork belly"));

        Item saved = save("Thịt ba chỉ", ItemCategory.INGREDIENT);

        assertThat(saved.getIngredientName()).isEqualTo("pork belly");
    }

    private Item save(String name, ItemCategory category) {
        SaveItemRequest request = new SaveItemRequest();
        request.setProductName(name);
        request.setExpiryDate("2030-01-01");
        request.setCategory(category);

        service.saveConfirmedProduct(request, 1L);

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(captor.capture());
        return captor.getValue();
    }
}
