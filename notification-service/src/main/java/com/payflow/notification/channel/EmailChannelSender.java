package com.payflow.notification.channel;

import com.payflow.notification.domain.enums.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailChannelSender implements ChannelSender {

    private static final Logger log = LoggerFactory.getLogger(EmailChannelSender.class);

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public boolean send(String recipient, String subject, String body) {
        log.info("[EMAIL DISPATCH] To: {} | Subject: {} | Body length: {}", recipient, subject, body.length());
        // Simulating SMTP / SES delivery
        return true;
    }
}
