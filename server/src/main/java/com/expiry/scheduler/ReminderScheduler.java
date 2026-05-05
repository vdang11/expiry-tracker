package com.expiry.scheduler;

import com.expiry.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final ReminderService reminderService;

    @Scheduled(cron = "${reminder.cron}")
    public void runDailyReminderJob() {
        int usersNotified = reminderService.sendDailyReminders();
        log.info("Reminder job completed. Users notified: {}", usersNotified);
    }
}