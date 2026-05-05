package com.expiry.service;

import com.expiry.entity.Item;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class ReminderTemplateBuilder {

    public String buildSubject() {
        return "Expiry reminder - Items needing attention";
    }

    public String buildHtmlBody(String userName, List<Item> items) {
        StringBuilder html = new StringBuilder();

        html.append("""
            <html>
            <body style="font-family: Arial, sans-serif; background:#f6f8fb; padding:20px;">
              <div style="max-width:700px; margin:0 auto; background:#ffffff; border-radius:12px; padding:24px;">
        """);

        html.append("<h2 style='color:#d97706; margin-top:0;'>Expiry Reminder</h2>");

        if (userName != null && !userName.isBlank()) {
            html.append("<p>Hello <strong>")
                    .append(escape(userName))
                    .append("</strong>,</p>");
        } else {
            html.append("<p>Hello,</p>");
        }

        html.append("<p>The following items are expired or expiring soon:</p>");

        LocalDate today = LocalDate.now();

        for (Item item : items) {

            long daysLeft = ChronoUnit.DAYS.between(today, item.getExpiryDate());

            String statusText;
            String statusColor;

            if (daysLeft < 0) {
                statusText = "EXPIRED";
                statusColor = "#dc2626";
            } else if (daysLeft == 0) {
                statusText = "EXPIRES TODAY";
                statusColor = "#ea580c";
            } else {
                statusText = "Expires in " + daysLeft + " day(s)";
                statusColor = "#2563eb";
            }

            html.append("""
                <div style="border:1px solid #e5e7eb; border-radius:10px; padding:16px; margin-bottom:16px;">
            """);

            html.append("<h3 style='margin:0 0 8px 0;'>")
                    .append(escape(item.getProductName()))
                    .append("</h3>");

            html.append("<p style='margin:4px 0;'><strong>Expiry date:</strong> ")
                    .append(item.getExpiryDate())
                    .append("</p>");

            html.append("<p style='margin:4px 0; color:")
                    .append(statusColor)
                    .append(";'><strong>Status:</strong> ")
                    .append(statusText)
                    .append("</p>");

            html.append("</div>");
        }

        html.append("""
                <p style="margin-top:24px;">Please review your items soon.</p>
              </div>
            </body>
            </html>
        """);

        return html.toString();
    }

    private String escape(String value) {
        if (value == null) return "";
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}