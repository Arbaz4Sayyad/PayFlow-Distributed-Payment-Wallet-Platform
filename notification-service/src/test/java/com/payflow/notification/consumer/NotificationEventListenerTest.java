package com.payflow.notification.consumer;

import com.payflow.common.model.currency.Currency;
import com.payflow.common.model.event.DomainEvent;
import com.payflow.common.model.event.payload.PaymentCompletedPayload;
import com.payflow.notification.dto.NotificationResponse;
import com.payflow.notification.dto.SendNotificationRequest;
import com.payflow.notification.inbox.NotificationInboxService;
import com.payflow.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventListener Deduplication & Event Tests")
class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationInboxService inboxService;

    @Mock
    private Acknowledgment acknowledgment;

    private NotificationEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new NotificationEventListener(notificationService, inboxService);
    }

    @Test
    @DisplayName("Should process fresh event, send notification, mark inbox, and acknowledge offset")
    void shouldProcessFreshEvent() {
        UUID paymentId = UUID.randomUUID();
        PaymentCompletedPayload payload = new PaymentCompletedPayload(
                paymentId, UUID.randomUUID(), UUID.randomUUID(),
                50000L, Currency.INR, "PGW-111"
        );

        DomainEvent<PaymentCompletedPayload> event = DomainEvent.of(
                "PaymentCompleted", "Payment", paymentId.toString(), payload
        );

        when(inboxService.isAlreadyProcessed(event.eventId(), NotificationEventListener.CONSUMER_NAME))
                .thenReturn(false);

        listener.onPaymentCompleted(event, acknowledgment);

        verify(notificationService).sendNotification(any(SendNotificationRequest.class));
        verify(inboxService).markProcessed(eq(event.eventId()), eq(NotificationEventListener.CONSUMER_NAME));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Should skip duplicate event and immediately acknowledge offset")
    void shouldSkipDuplicateEvent() {
        UUID paymentId = UUID.randomUUID();
        PaymentCompletedPayload payload = new PaymentCompletedPayload(
                paymentId, UUID.randomUUID(), UUID.randomUUID(),
                50000L, Currency.INR, "PGW-DUP"
        );

        DomainEvent<PaymentCompletedPayload> event = DomainEvent.of(
                "PaymentCompleted", "Payment", paymentId.toString(), payload
        );

        when(inboxService.isAlreadyProcessed(event.eventId(), NotificationEventListener.CONSUMER_NAME))
                .thenReturn(true);

        listener.onPaymentCompleted(event, acknowledgment);

        verify(notificationService, never()).sendNotification(any());
        verify(inboxService, never()).markProcessed(any(), any());
        verify(acknowledgment).acknowledge();
    }
}
