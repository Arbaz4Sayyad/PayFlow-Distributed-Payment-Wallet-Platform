package com.payflow.notification.service;

import com.payflow.notification.channel.ChannelSender;
import com.payflow.notification.domain.entity.NotificationLog;
import com.payflow.notification.domain.enums.NotificationChannel;
import com.payflow.notification.domain.enums.NotificationStatus;
import com.payflow.notification.domain.repository.NotificationLogRepository;
import com.payflow.notification.dto.NotificationResponse;
import com.payflow.notification.dto.SendNotificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Multi-Channel Dispatch Tests")
class NotificationServiceTest {

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private ChannelSender emailSender;

    @Mock
    private ChannelSender smsSender;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        when(emailSender.getChannel()).thenReturn(NotificationChannel.EMAIL);
        when(smsSender.getChannel()).thenReturn(NotificationChannel.SMS);

        notificationService = new NotificationService(
                List.of(emailSender, smsSender),
                notificationLogRepository
        );
        when(notificationLogRepository.save(any(NotificationLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should successfully send email notification and mark SENT")
    void shouldSendEmailNotification() {
        when(emailSender.send(anyString(), anyString(), anyString())).thenReturn(true);

        SendNotificationRequest request = new SendNotificationRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                NotificationChannel.EMAIL,
                "user@example.com",
                "Payment Alert",
                "Your payment was successful."
        );

        NotificationResponse response = notificationService.sendNotification(request);

        assertThat(response.status()).isEqualTo(NotificationStatus.SENT);
        assertThat(response.sentAt()).isNotNull();
        verify(emailSender).send("user@example.com", "Payment Alert", "Your payment was successful.");
    }

    @Test
    @DisplayName("Should mark FAILED when channel provider fails to deliver")
    void shouldHandleChannelDeliveryFailure() {
        when(smsSender.send(anyString(), anyString(), anyString())).thenReturn(false);

        SendNotificationRequest request = new SendNotificationRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                NotificationChannel.SMS,
                "+919876543210",
                null,
                "Your OTP is 123456"
        );

        NotificationResponse response = notificationService.sendNotification(request);

        assertThat(response.status()).isEqualTo(NotificationStatus.FAILED);
        assertThat(response.sentAt()).isNull();
    }
}
