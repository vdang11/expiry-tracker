package com.expiry.service;

import com.expiry.config.ReminderProperties;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReminderEmailService {

    private final JavaMailSender mailSender;
    private final ReminderProperties reminderProperties;

    public void sendReminderEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setFrom(reminderProperties.getFromEmail());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);

        } catch (Exception ex) {
            throw new IllegalStateException("Failed to send reminder email", ex);
        }
    }
}