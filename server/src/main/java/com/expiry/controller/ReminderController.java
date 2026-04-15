package com.expiry.controller;

import com.expiry.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping("/send-now")
    public Map<String, Object> sendNow() {
        int usersNotified = reminderService.sendDailyReminders();

        return Map.of(
                "message", "Reminder job executed",
                "usersNotified", usersNotified
        );
    }
}