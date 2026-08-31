package com.payflow.notification.consumer;

import com.payflow.common.model.currency.Money;
import com.payflow.common.model.event.DomainEvent;
import com.payflow.common.model.event.KafkaTopics;
import com.payflow.common.model.event.payload.PaymentCompletedPayload;
import com.payflow.notification.domain.enums.NotificationChannel;
import com.payflow.notification.dto.SendNotificationRequest;
import com.payflow.notification.inbox.NotificationInboxService;
import com.payflow.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);
    public static final String CONSUMER_NAME = "notification-payment-completed-consumer";

    private final NotificationService notificationService;
    private final NotificationInboxService inboxService;

    public NotificationEventListener(
            NotificationService notificationService,
            NotificationInboxService inboxService
    ) {
        this.notificationService = notificationService;
        this.inboxService = inboxService;
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_COMPLETED,
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void onPaymentCompleted(DomainEvent<PaymentCompletedPayload> event, Acknowledgment acknowledgment) {
        log.info("Notification Service received PaymentCompleted event {} for payment {}",
                event.eventId(), event.payload().paymentId());

        // Deduplication guard
        if (inboxService.isAlreadyProcessed(event.eventId(), CONSUMER_NAME)) {
            log.info("Duplicate notification event {} detected. Acknowledging offset and skipping.", event.eventId());
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            return;
        }

        PaymentCompletedPayload payload = event.payload();
        Money money = Money.of(payload.amountMinor(), payload.currency());

        SendNotificationRequest emailRequest = new SendNotificationRequest(
                event.eventId(),
                payload.senderWalletId(),
                NotificationChannel.EMAIL,
                "customer-" + payload.senderWalletId() + "@payflow.internal",
                "Payment Receipt - " + payload.paymentId(),
                "Your payment of " + money.formatDisplay() + " has been successfully completed. Ref: " + payload.gatewayTransactionId()
        );

        notificationService.sendNotification(emailRequest);
        inboxService.markProcessed(event.eventId(), CONSUMER_NAME);

        log.info("Notification dispatched and recorded for event {}", event.eventId());
        if (acknowledgment != null) {
            acknowledgment.acknowledge();
        }
    }
}
