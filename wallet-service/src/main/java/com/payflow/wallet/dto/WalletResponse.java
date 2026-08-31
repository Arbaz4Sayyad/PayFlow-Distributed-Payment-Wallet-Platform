package com.payflow.wallet.dto;

import com.payflow.common.model.currency.Currency;
import com.payflow.common.model.currency.Money;
import com.payflow.common.model.enums.WalletStatus;
import com.payflow.wallet.domain.entity.Wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        UUID userId,
        Currency currency,
        BigDecimal balance,
        long balanceMinor,
        WalletStatus status,
        Instant createdAt
) {
    public static WalletResponse fromEntity(Wallet wallet) {
        Money money = Money.of(wallet.getBalanceMinor(), wallet.getCurrency());
        return new WalletResponse(
                wallet.getId(),
                wallet.getUserId(),
                wallet.getCurrency(),
                money.toBigDecimal(),
                wallet.getBalanceMinor(),
                wallet.getStatus(),
                wallet.getCreatedAt()
        );
    }
}
