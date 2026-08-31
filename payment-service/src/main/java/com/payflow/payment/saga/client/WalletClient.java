package com.payflow.payment.saga.client;

import com.payflow.common.model.currency.Currency;

import java.util.UUID;

public interface WalletClient {

    boolean debit(UUID walletId, long amountMinor, Currency currency, String referenceId);

    boolean credit(UUID walletId, long amountMinor, Currency currency, String referenceId);
}
