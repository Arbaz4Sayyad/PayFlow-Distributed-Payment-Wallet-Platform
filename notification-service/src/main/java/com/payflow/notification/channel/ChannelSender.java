package com.payflow.notification.channel;

import com.payflow.notification.domain.enums.NotificationChannel;

public interface ChannelSender {

    NotificationChannel getChannel();

    /**
     * Sends the notification to the provider or gateway.
     * Returns true if successfully accepted by the downstream channel provider.
     */
    boolean send(String recipient, String subject, String body);
}
