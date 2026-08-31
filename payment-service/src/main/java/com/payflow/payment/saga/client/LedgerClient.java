package com.payflow.payment.saga.client;

import com.payflow.common.model.currency.Currency;

import java.util.UUID;

public interface LedgerClient {

    boolean recordEntry(UUID transactionId, UUID senderWalletId, UUID recipientWalletId, long amountMinor, Currency currency);
}
