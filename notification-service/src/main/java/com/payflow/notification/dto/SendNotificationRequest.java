package com.payflow.notification.dto;

import com.payflow.notification.domain.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SendNotificationRequest(
        @NotNull(message = "Event ID is required")
        UUID eventId,

        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Channel is required")
        NotificationChannel channel,

        @NotBlank(message = "Recipient is required")
        String recipient,

        String subject,

        @NotBlank(message = "Body is required")
        String body
) {
}
