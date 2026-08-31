package com.payflow.notification.channel;

import com.payflow.notification.domain.enums.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SmsChannelSender implements ChannelSender {

    private static final Logger log = LoggerFactory.getLogger(SmsChannelSender.class);

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public boolean send(String recipient, String subject, String body) {
        log.info("[SMS DISPATCH] To: {} | Body: {}", recipient, body);
        // Simulating Twilio / SNS delivery
        return true;
    }
}
