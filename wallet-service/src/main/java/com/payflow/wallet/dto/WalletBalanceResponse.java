package com.payflow.wallet.dto;

import com.payflow.common.model.currency.Currency;
import com.payflow.common.model.currency.Money;
import com.payflow.wallet.domain.entity.Wallet;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletBalanceResponse(
        UUID walletId,
        Currency currency,
        BigDecimal balance,
        long balanceMinor,
        String formattedBalance
) {
    public static WalletBalanceResponse fromEntity(Wallet wallet) {
        Money money = Money.of(wallet.getBalanceMinor(), wallet.getCurrency());
        return new WalletBalanceResponse(
                wallet.getId(),
                wallet.getCurrency(),
                money.toBigDecimal(),
                wallet.getBalanceMinor(),
                money.formatDisplay()
        );
    }
}
