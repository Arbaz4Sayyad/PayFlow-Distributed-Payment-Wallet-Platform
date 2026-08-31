-- PayFlow Immutable Double-Entry Ledger Schema
-- Database: ledger_db

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE journal_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL,
    description VARCHAR(255) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_journal_entries_tx UNIQUE (transaction_id)
);

CREATE TABLE journal_entry_lines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    journal_entry_id UUID NOT NULL REFERENCES journal_entries(id) ON DELETE CASCADE,
    wallet_id UUID NOT NULL,
    entry_type VARCHAR(16) NOT NULL,
    amount_minor BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_positive_ledger_amount CHECK (amount_minor > 0),
    CONSTRAINT chk_entry_type CHECK (entry_type IN ('DEBIT', 'CREDIT'))
);

-- Consumer Inbox for Kafka Message Deduplication
CREATE TABLE processed_events (
    event_id UUID NOT NULL,
    consumer_name VARCHAR(64) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (event_id, consumer_name)
);

CREATE INDEX idx_journal_entries_tx ON journal_entries(transaction_id);
CREATE INDEX idx_journal_lines_wallet_created ON journal_entry_lines(wallet_id, created_at DESC);
CREATE INDEX idx_journal_lines_entry_id ON journal_entry_lines(journal_entry_id);
