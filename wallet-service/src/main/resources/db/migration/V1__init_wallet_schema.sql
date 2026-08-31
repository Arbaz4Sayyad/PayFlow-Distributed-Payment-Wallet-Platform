-- PayFlow Wallet Schema
-- Database: wallet_db

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE wallets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    currency VARCHAR(3) NOT NULL,
    balance_minor BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_wallets_user UNIQUE (user_id),
    CONSTRAINT chk_wallet_balance_positive CHECK (balance_minor >= 0),
    CONSTRAINT chk_wallet_status CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED'))
);

CREATE TABLE wallet_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id UUID NOT NULL REFERENCES wallets(id),
    operation VARCHAR(32) NOT NULL, -- TOP_UP, WITHDRAW, DEBIT, CREDIT, FREEZE, UNFREEZE
    amount_minor BIGINT NOT NULL,
    balance_before_minor BIGINT NOT NULL,
    balance_after_minor BIGINT NOT NULL,
    reference_id VARCHAR(128),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_wallets_user_id ON wallets(user_id);
CREATE INDEX idx_wallets_status ON wallets(status);
CREATE INDEX idx_wallet_audit_wallet_created ON wallet_audit_log(wallet_id, created_at DESC);
