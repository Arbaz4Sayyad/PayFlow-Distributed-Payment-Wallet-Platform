-- PayFlow Fraud & Risk Schema
-- Database: fraud_db

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE fraud_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_name VARCHAR(64) NOT NULL,
    description VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    threshold_value NUMERIC(18, 4),
    action VARCHAR(32) NOT NULL, -- APPROVE, REVIEW, REJECT
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_fraud_rules_name UNIQUE (rule_name),
    CONSTRAINT chk_fraud_action CHECK (action IN ('APPROVE', 'REVIEW', 'REJECT'))
);

CREATE TABLE blacklist_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    target_type VARCHAR(32) NOT NULL, -- USER, MERCHANT, IP, DEVICE_ID
    target_value VARCHAR(255) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_blacklist_target UNIQUE (target_type, target_value)
);

CREATE TABLE flagged_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL,
    user_id UUID NOT NULL,
    risk_score INT NOT NULL,
    decision VARCHAR(32) NOT NULL, -- APPROVED, REVIEW, REJECTED
    triggered_rules TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_flagged_decision CHECK (decision IN ('APPROVED', 'REVIEW', 'REJECTED'))
);

CREATE INDEX idx_blacklist_lookup ON blacklist_records(target_type, target_value);
CREATE INDEX idx_flagged_tx ON flagged_transactions(transaction_id);
CREATE INDEX idx_flagged_user ON flagged_transactions(user_id, created_at DESC);
