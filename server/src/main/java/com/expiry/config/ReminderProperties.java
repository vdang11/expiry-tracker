package com.expiry.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "reminder")
public class ReminderProperties {

    private int windowDays = 3;
    private String cron = "0 0 8 * * *";
    private String fromEmail = "noreply@expiry.com";
}