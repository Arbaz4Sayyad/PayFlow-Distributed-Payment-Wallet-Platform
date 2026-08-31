package com.payflow.notification.service;

import com.payflow.notification.channel.ChannelSender;
import com.payflow.notification.domain.entity.NotificationLog;
import com.payflow.notification.domain.enums.NotificationChannel;
import com.payflow.notification.domain.repository.NotificationLogRepository;
import com.payflow.notification.dto.NotificationResponse;
import com.payflow.notification.dto.SendNotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final Map<NotificationChannel, ChannelSender> channelSenders;
    private final NotificationLogRepository notificationLogRepository;

    public NotificationService(
            List<ChannelSender> senders,
            NotificationLogRepository notificationLogRepository
    ) {
        this.channelSenders = senders.stream()
                .collect(Collectors.toMap(ChannelSender::getChannel, Function.identity()));
        this.notificationLogRepository = notificationLogRepository;
    }

    /**
     * Dispatches notification across the requested delivery channel with persistent audit logging.
     */
    @Transactional
    public NotificationResponse sendNotification(SendNotificationRequest request) {
        NotificationLog notificationLog = new NotificationLog(
                request.eventId(),
                request.userId(),
                request.channel(),
                request.recipient(),
                request.subject(),
                request.body()
        );
        notificationLog = notificationLogRepository.save(notificationLog);

        ChannelSender sender = channelSenders.get(request.channel());
        if (sender == null) {
            log.error("No sender configured for channel {}", request.channel());
            notificationLog.recordFailure("Unsupported channel: " + request.channel());
            notificationLogRepository.save(notificationLog);
            return NotificationResponse.fromEntity(notificationLog);
        }

        try {
            boolean success = sender.send(request.recipient(), request.subject(), request.body());
            if (success) {
                notificationLog.markSent();
                log.info("Notification {} sent successfully via {}", notificationLog.getId(), request.channel());
            } else {
                notificationLog.recordFailure("Channel provider failed to deliver");
                log.warn("Notification {} delivery failed via {}", notificationLog.getId(), request.channel());
            }
        } catch (Exception ex) {
            log.error("Exception during notification delivery: {}", ex.getMessage(), ex);
            notificationLog.recordFailure(ex.getMessage());
        }

        notificationLog = notificationLogRepository.save(notificationLog);
        return NotificationResponse.fromEntity(notificationLog);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationHistory(UUID userId) {
        return notificationLogRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }
}
