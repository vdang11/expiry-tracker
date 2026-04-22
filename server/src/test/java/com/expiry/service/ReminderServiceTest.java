package com.expiry.service;

import com.expiry.config.ReminderProperties;
import com.expiry.entity.Item;
import com.expiry.entity.User;
import com.expiry.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ReminderServiceTest {

    @Mock
    private ExpiryReminderQueryService queryService;

    @Mock
    private ReminderTemplateBuilder templateBuilder;

    @Mock
    private ReminderEmailService emailService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ReminderProperties reminderProperties;

    @InjectMocks
    private ReminderService reminderService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(reminderProperties.getWindowDays()).thenReturn(3);
    }

    // 🔥 CASE 1: Có item → gửi mail
    @Test
    void shouldSendEmail_whenItemsExist() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");
        user.setName("Vinh");

        Item item = new Item();
        item.setId(1L);
        item.setUser(user);
        item.setProductName("Milk");
        item.setExpiryDate(LocalDate.now());
        item.setItemStatus("ACTIVE");

        when(queryService.findExpiredItems()).thenReturn(List.of());
        when(queryService.findExpiringSoonItems(3)).thenReturn(List.of(item));

        when(templateBuilder.buildSubject()).thenReturn("Test Subject");
        when(templateBuilder.buildHtmlBody(any(), any())).thenReturn("Test Body");

        int result = reminderService.sendDailyReminders();

        assertEquals(1, result);
        verify(emailService, times(1))
                .sendReminderEmail(eq("test@gmail.com"), any(), any());
    }

    // 🔥 CASE 2: Không có item → không gửi
    @Test
    void shouldNotSendEmail_whenNoItems() {

        when(queryService.findExpiredItems()).thenReturn(List.of());
        when(queryService.findExpiringSoonItems(3)).thenReturn(List.of());

        int result = reminderService.sendDailyReminders();

        assertEquals(0, result);
        verify(emailService, never()).sendReminderEmail(any(), any(), any());
    }

    // 🔥 CASE 3: Đã gửi hôm nay → không gửi lại
    @Test
    void shouldNotSendEmail_whenAlreadySentToday() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");

        Item item = new Item();
        item.setId(1L);
        item.setUser(user);
        item.setExpiryDate(LocalDate.now());
        item.setItemStatus("ACTIVE");
        item.setLastReminderSentDate(LocalDate.now()); // 🔥 đã gửi hôm nay

        when(queryService.findExpiredItems()).thenReturn(List.of());
        when(queryService.findExpiringSoonItems(3)).thenReturn(List.of(item));

        int result = reminderService.sendDailyReminders();

        assertEquals(0, result);
        verify(emailService, never()).sendReminderEmail(any(), any(), any());
    }
}