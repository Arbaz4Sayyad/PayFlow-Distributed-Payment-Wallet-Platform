package com.payflow.notification.channel;

import com.payflow.notification.domain.enums.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PushChannelSender implements ChannelSender {

    private static final Logger log = LoggerFactory.getLogger(PushChannelSender.class);

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public boolean send(String recipient, String subject, String body) {
        log.info("[PUSH DISPATCH] Token: {} | Title: {} | Message: {}", recipient, subject, body);
        // Simulating Firebase Cloud Messaging (FCM) / APNs delivery
        return true;
    }
}
