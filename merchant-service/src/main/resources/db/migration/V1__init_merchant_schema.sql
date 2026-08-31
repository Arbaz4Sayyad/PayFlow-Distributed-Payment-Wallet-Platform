-- PayFlow Merchant Schema
-- Database: merchant_db

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE merchants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    business_name VARCHAR(255) NOT NULL,
    business_type VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    settlement_bank_account VARCHAR(64),
    settlement_ifsc_routing VARCHAR(32),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_merchants_user UNIQUE (user_id),
    CONSTRAINT chk_merchant_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'TERMINATED'))
);

CREATE TABLE merchant_api_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    api_key_hash VARCHAR(255) NOT NULL,
    label VARCHAR(64) NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_merchant_api_key_hash UNIQUE (api_key_hash)
);

CREATE INDEX idx_merchants_user_id ON merchants(user_id);
CREATE INDEX idx_merchant_api_keys_hash ON merchant_api_keys(api_key_hash) WHERE revoked = FALSE;
