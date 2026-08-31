package com.payflow.wallet.service;

import com.payflow.common.model.currency.Currency;
import com.payflow.common.model.enums.WalletStatus;
import com.payflow.common.model.exception.CurrencyMismatchException;
import com.payflow.common.model.exception.InsufficientFundsException;
import com.payflow.common.model.exception.PayFlowException;
import com.payflow.common.model.exception.WalletFrozenException;
import com.payflow.wallet.domain.entity.Wallet;
import com.payflow.wallet.domain.entity.WalletAuditLog;
import com.payflow.wallet.domain.repository.WalletAuditLogRepository;
import com.payflow.wallet.domain.repository.WalletRepository;
import com.payflow.wallet.dto.CreateWalletRequest;
import com.payflow.wallet.dto.WalletBalanceResponse;
import com.payflow.wallet.dto.WalletOperationRequest;
import com.payflow.wallet.dto.WalletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletService Concurrency & Invariant Unit Tests")
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletAuditLogRepository auditLogRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletService = new WalletService(walletRepository, auditLogRepository, redisTemplate);
    }

    @Test
    @DisplayName("Should create wallet when user has no existing wallet")
    void shouldCreateWalletSuccessfully() {
        UUID userId = UUID.randomUUID();
        CreateWalletRequest request = new CreateWalletRequest(Currency.INR);

        when(walletRepository.existsByUserId(userId)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WalletResponse response = walletService.createWallet(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.currency()).isEqualTo(Currency.INR);
        assertThat(response.balanceMinor()).isEqualTo(0L);

        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Should reject duplicate wallet creation for same user")
    void shouldRejectDuplicateWallet() {
        UUID userId = UUID.randomUUID();
        when(walletRepository.existsByUserId(userId)).thenReturn(true);

        assertThatThrownBy(() -> walletService.createWallet(userId, new CreateWalletRequest(Currency.INR)))
                .isInstanceOf(PayFlowException.class)
                .hasMessageContaining("User already owns an active wallet");

        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Should atomically top-up funds and record audit log")
    void shouldTopUpSuccessfully() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, userId, Currency.INR);

        WalletOperationRequest request = new WalletOperationRequest(
                new BigDecimal("500.00"),
                Currency.INR,
                "REF-TOPUP-101",
                "Salary deposit"
        );

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.executeAtomicCredit(eq(walletId), eq(50000L), any(Instant.class))).thenReturn(1);

        WalletBalanceResponse response = walletService.topUp(walletId, request);

        assertThat(response).isNotNull();
        assertThat(response.currency()).isEqualTo(Currency.INR);
        verify(walletRepository).executeAtomicCredit(eq(walletId), eq(50000L), any(Instant.class));
        verify(auditLogRepository).save(any(WalletAuditLog.class));
    }

    @Test
    @DisplayName("Should atomically withdraw funds when balance is sufficient")
    void shouldWithdrawSuccessfully() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, userId, Currency.INR);
        wallet.setBalanceMinor(100000L); // ₹1000.00

        WalletOperationRequest request = new WalletOperationRequest(
                new BigDecimal("400.00"),
                Currency.INR,
                "REF-WITHDRAW-202",
                "ATM withdrawal"
        );

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.executeAtomicDebit(eq(walletId), eq(40000L), any(Instant.class))).thenReturn(1);

        WalletBalanceResponse response = walletService.withdraw(walletId, request);

        assertThat(response).isNotNull();
        verify(walletRepository).executeAtomicDebit(eq(walletId), eq(40000L), any(Instant.class));
        verify(auditLogRepository).save(any(WalletAuditLog.class));
    }

    @Test
    @DisplayName("Should throw InsufficientFundsException when atomic conditional debit updates 0 rows")
    void shouldThrowInsufficientFundsWhenAtomicDebitFails() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, userId, Currency.INR);
        wallet.setBalanceMinor(20000L); // ₹200.00

        WalletOperationRequest request = new WalletOperationRequest(
                new BigDecimal("800.00"), // Request ₹800.00
                Currency.INR,
                "REF-FAIL-1",
                "Overdraft attempt"
        );

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        // Atomic debit conditional update fails because balance_minor < amount
        when(walletRepository.executeAtomicDebit(eq(walletId), eq(80000L), any(Instant.class))).thenReturn(0);

        assertThatThrownBy(() -> walletService.withdraw(walletId, request))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("has insufficient funds");

        verify(auditLogRepository, never()).save(any(WalletAuditLog.class));
    }

    @Test
    @DisplayName("Should reject withdrawal on FROZEN wallet")
    void shouldRejectWithdrawalOnFrozenWallet() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, userId, Currency.INR);
        wallet.setStatus(WalletStatus.FROZEN);

        WalletOperationRequest request = new WalletOperationRequest(
                new BigDecimal("100.00"),
                Currency.INR,
                "REF-FAIL-FROZEN",
                "Frozen debit"
        );

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.withdraw(walletId, request))
                .isInstanceOf(WalletFrozenException.class)
                .hasMessageContaining("is FROZEN and cannot execute transactions");

        verify(walletRepository, never()).executeAtomicDebit(any(), anyLong(), any());
    }

    @Test
    @DisplayName("Should reject operation when currency does not match wallet currency")
    void shouldRejectCurrencyMismatch() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, userId, Currency.INR);

        WalletOperationRequest request = new WalletOperationRequest(
                new BigDecimal("100.00"),
                Currency.USD, // Wrong currency
                "REF-FAIL-CURR",
                "USD debit on INR wallet"
        );

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.withdraw(walletId, request))
                .isInstanceOf(CurrencyMismatchException.class)
                .hasMessageContaining("Operation currency mismatch");
    }
}
