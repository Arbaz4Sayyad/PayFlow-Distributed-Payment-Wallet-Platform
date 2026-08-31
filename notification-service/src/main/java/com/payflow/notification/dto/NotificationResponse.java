package com.payflow.notification.dto;

import com.payflow.notification.domain.entity.NotificationLog;
import com.payflow.notification.domain.enums.NotificationChannel;
import com.payflow.notification.domain.enums.NotificationStatus;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID eventId,
        UUID userId,
        NotificationChannel channel,
        String recipient,
        String subject,
        String body,
        NotificationStatus status,
        Instant createdAt,
        Instant sentAt
) {
    public static NotificationResponse fromEntity(NotificationLog log) {
        return new NotificationResponse(
                log.getId(),
                log.getEventId(),
                log.getUserId(),
                log.getChannel(),
                log.getRecipient(),
                log.getSubject(),
                log.getBody(),
                log.getStatus(),
                log.getCreatedAt(),
                log.getSentAt()
        );
    }
}
