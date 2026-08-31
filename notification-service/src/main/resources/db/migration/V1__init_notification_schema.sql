-- PayFlow Notification Schema
-- Database: notification_db

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE notification_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    user_id UUID NOT NULL,
    channel VARCHAR(32) NOT NULL, -- EMAIL, SMS, PUSH, WEBHOOK
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    body TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    last_error TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_notification_channel CHECK (channel IN ('EMAIL', 'SMS', 'PUSH', 'WEBHOOK')),
    CONSTRAINT chk_notification_status CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);

-- Consumer Inbox for Kafka Message Deduplication
CREATE TABLE processed_events (
    event_id UUID NOT NULL,
    consumer_name VARCHAR(64) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (event_id, consumer_name)
);

CREATE INDEX idx_notification_logs_user_created ON notification_logs(user_id, created_at DESC);
CREATE INDEX idx_notification_logs_status ON notification_logs(status);
