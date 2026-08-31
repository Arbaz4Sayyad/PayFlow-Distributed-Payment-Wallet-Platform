-- PayFlow Payment & Saga Schema
-- Database: payment_db

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_wallet_id UUID NOT NULL,
    recipient_wallet_id UUID NOT NULL,
    amount_minor BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(32) NOT NULL,
    payment_type VARCHAR(32) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    failure_reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_payment_idempotency UNIQUE (sender_wallet_id, idempotency_key),
    CONSTRAINT chk_payment_amount_positive CHECK (amount_minor > 0),
    CONSTRAINT chk_payment_status CHECK (status IN (
        'CREATED', 'PROCESSING', 'SUCCESS', 'FAILED', 'CANCELLED', 'REFUND_PENDING', 'REFUNDED'
    )),
    CONSTRAINT chk_payment_type CHECK (payment_type IN (
        'WALLET_TRANSFER', 'MERCHANT_PAYMENT', 'TOP_UP', 'WITHDRAWAL', 'REFUND'
    ))
);

CREATE TABLE idempotency_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(128) NOT NULL,
    user_id UUID NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    response_body TEXT,
    status_code INT,
    status VARCHAR(32) NOT NULL, -- PENDING, COMPLETED, FAILED
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_user_idempotency_key UNIQUE (user_id, idempotency_key),
    CONSTRAINT chk_idempotency_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'))
);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_outbox_status CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE TABLE saga_instances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    saga_type VARCHAR(64) NOT NULL,
    correlation_id VARCHAR(128) NOT NULL,
    current_state VARCHAR(64) NOT NULL,
    current_step VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    payload TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_saga_correlation UNIQUE (correlation_id),
    CONSTRAINT chk_saga_status CHECK (status IN (
        'STARTED', 'PROCESSING', 'COMPENSATING', 'COMPENSATED', 'COMPLETED', 'FAILED'
    ))
);

CREATE INDEX idx_payments_sender ON payments(sender_wallet_id, created_at DESC);
CREATE INDEX idx_payments_recipient ON payments(recipient_wallet_id, created_at DESC);
CREATE INDEX idx_payments_status_created ON payments(status, created_at DESC);

CREATE INDEX idx_idempotency_lookup ON idempotency_records(user_id, idempotency_key);
CREATE INDEX idx_idempotency_expiry ON idempotency_records(expires_at);

CREATE INDEX idx_outbox_pending ON outbox_events(status, created_at) WHERE status = 'PENDING';
CREATE INDEX idx_saga_correlation ON saga_instances(correlation_id);
