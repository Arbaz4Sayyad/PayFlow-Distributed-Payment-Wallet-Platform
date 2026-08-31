package com.payflow.wallet.service;

import com.payflow.common.model.currency.Currency;
import com.payflow.common.model.currency.Money;
import com.payflow.common.model.enums.WalletStatus;
import com.payflow.common.model.exception.CurrencyMismatchException;
import com.payflow.common.model.exception.InsufficientFundsException;
import com.payflow.common.model.exception.PayFlowException;
import com.payflow.common.model.exception.WalletFrozenException;
import com.payflow.common.model.exception.WalletNotFoundException;
import com.payflow.wallet.domain.entity.Wallet;
import com.payflow.wallet.domain.entity.WalletAuditLog;
import com.payflow.wallet.domain.repository.WalletAuditLogRepository;
import com.payflow.wallet.domain.repository.WalletRepository;
import com.payflow.wallet.dto.CreateWalletRequest;
import com.payflow.wallet.dto.WalletBalanceResponse;
import com.payflow.wallet.dto.WalletOperationRequest;
import com.payflow.wallet.dto.WalletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);
    private static final String CACHE_KEY_PREFIX = "wallet:balance:";

    private final WalletRepository walletRepository;
    private final WalletAuditLogRepository auditLogRepository;
    private final StringRedisTemplate redisTemplate;

    public WalletService(
            WalletRepository walletRepository,
            WalletAuditLogRepository auditLogRepository,
            StringRedisTemplate redisTemplate
    ) {
        this.walletRepository = walletRepository;
        this.auditLogRepository = auditLogRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public WalletResponse createWallet(UUID userId, CreateWalletRequest request) {
        if (walletRepository.existsByUserId(userId)) {
            throw new PayFlowException("WALLET_ALREADY_EXISTS", "User already owns an active wallet", 409);
        }

        Currency currency = request != null && request.currency() != null ? request.currency() : Currency.INR;
        Wallet wallet = new Wallet(userId, currency);
        wallet = walletRepository.save(wallet);

        log.info("Wallet created successfully. WalletId: {}, UserId: {}, Currency: {}",
                wallet.getId(), userId, currency);

        return WalletResponse.fromEntity(wallet);
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID walletId) {
        Wallet wallet = findWalletOrThrow(walletId);
        return WalletResponse.fromEntity(wallet);
    }

    @Transactional(readOnly = true)
    public WalletBalanceResponse getBalance(UUID walletId) {
        Wallet wallet = findWalletOrThrow(walletId);
        return WalletBalanceResponse.fromEntity(wallet);
    }

    @Transactional
    public WalletBalanceResponse topUp(UUID walletId, WalletOperationRequest request) {
        Wallet wallet = findWalletOrThrow(walletId);
        validateWalletActive(wallet);
        validateCurrency(wallet, request.currency());

        Money money = Money.fromBigDecimal(request.amount(), request.currency());
        long amountMinor = money.amountMinor();
        long balanceBefore = wallet.getBalanceMinor();

        Instant now = Instant.now();
        int rowsUpdated = walletRepository.executeAtomicCredit(walletId, amountMinor, now);
        if (rowsUpdated == 0) {
            throw new WalletFrozenException(walletId, wallet.getStatus().name());
        }

        long balanceAfter = balanceBefore + amountMinor;
        recordAudit(wallet, "TOP_UP", amountMinor, balanceBefore, balanceAfter, request.referenceId());
        evictCache(walletId);

        log.info("Wallet topped up. WalletId: {}, Added: {}, New Balance: {}",
                walletId, money.formatDisplay(), balanceAfter);

        Wallet updatedWallet = findWalletOrThrow(walletId);
        return WalletBalanceResponse.fromEntity(updatedWallet);
    }

    @Transactional
    public WalletBalanceResponse withdraw(UUID walletId, WalletOperationRequest request) {
        Wallet wallet = findWalletOrThrow(walletId);
        validateWalletActive(wallet);
        validateCurrency(wallet, request.currency());

        Money money = Money.fromBigDecimal(request.amount(), request.currency());
        long amountMinor = money.amountMinor();
        long balanceBefore = wallet.getBalanceMinor();

        Instant now = Instant.now();
        int rowsUpdated = walletRepository.executeAtomicDebit(walletId, amountMinor, now);
        if (rowsUpdated == 0) {
            // Re-read to diagnose exact root cause
            Wallet current = findWalletOrThrow(walletId);
            if (!current.isActive()) {
                throw new WalletFrozenException(walletId, current.getStatus().name());
            }
            throw new InsufficientFundsException(
                    walletId,
                    money.toBigDecimal(),
                    Money.of(current.getBalanceMinor(), current.getCurrency()).toBigDecimal()
            );
        }

        long balanceAfter = balanceBefore - amountMinor;
        recordAudit(wallet, "WITHDRAW", amountMinor, balanceBefore, balanceAfter, request.referenceId());
        evictCache(walletId);

        log.info("Wallet withdrawal executed. WalletId: {}, Deducted: {}, Remaining: {}",
                walletId, money.formatDisplay(), balanceAfter);

        Wallet updatedWallet = findWalletOrThrow(walletId);
        return WalletBalanceResponse.fromEntity(updatedWallet);
    }

    @Transactional
    public void executeAtomicDebit(UUID walletId, long amountMinor, Currency currency, String referenceId) {
        Wallet wallet = findWalletOrThrow(walletId);
        validateWalletActive(wallet);
        validateCurrency(wallet, currency);

        long balanceBefore = wallet.getBalanceMinor();
        int rowsUpdated = walletRepository.executeAtomicDebit(walletId, amountMinor, Instant.now());
        if (rowsUpdated == 0) {
            Wallet current = findWalletOrThrow(walletId);
            if (!current.isActive()) {
                throw new WalletFrozenException(walletId, current.getStatus().name());
            }
            throw new InsufficientFundsException(
                    walletId,
                    Money.of(amountMinor, currency).toBigDecimal(),
                    Money.of(current.getBalanceMinor(), current.getCurrency()).toBigDecimal()
            );
        }

        long balanceAfter = balanceBefore - amountMinor;
        recordAudit(wallet, "DEBIT", amountMinor, balanceBefore, balanceAfter, referenceId);
        evictCache(walletId);
    }

    @Transactional
    public void executeAtomicCredit(UUID walletId, long amountMinor, Currency currency, String referenceId) {
        Wallet wallet = findWalletOrThrow(walletId);
        validateWalletActive(wallet);
        validateCurrency(wallet, currency);

        long balanceBefore = wallet.getBalanceMinor();
        int rowsUpdated = walletRepository.executeAtomicCredit(walletId, amountMinor, Instant.now());
        if (rowsUpdated == 0) {
            throw new WalletFrozenException(walletId, wallet.getStatus().name());
        }

        long balanceAfter = balanceBefore + amountMinor;
        recordAudit(wallet, "CREDIT", amountMinor, balanceBefore, balanceAfter, referenceId);
        evictCache(walletId);
    }

    @Transactional
    public WalletResponse updateWalletStatus(UUID walletId, WalletStatus newStatus) {
        Wallet wallet = findWalletOrThrow(walletId);
        wallet.setStatus(newStatus);
        wallet = walletRepository.save(wallet);

        recordAudit(wallet, newStatus.name(), 0L, wallet.getBalanceMinor(), wallet.getBalanceMinor(), "STATUS_CHANGE");
        evictCache(walletId);

        log.info("Wallet status updated. WalletId: {}, New Status: {}", walletId, newStatus);
        return WalletResponse.fromEntity(wallet);
    }

    private Wallet findWalletOrThrow(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
    }

    private void validateWalletActive(Wallet wallet) {
        if (!wallet.isActive()) {
            throw new WalletFrozenException(wallet.getId(), wallet.getStatus().name());
        }
    }

    private void validateCurrency(Wallet wallet, Currency currency) {
        if (wallet.getCurrency() != currency) {
            throw new CurrencyMismatchException(wallet.getCurrency(), currency);
        }
    }

    private void recordAudit(Wallet wallet, String operation, long amountMinor, long before, long after, String refId) {
        WalletAuditLog logEntry = new WalletAuditLog(wallet, operation, amountMinor, before, after, refId);
        auditLogRepository.save(logEntry);
    }

    private void evictCache(UUID walletId) {
        try {
            if (redisTemplate != null) {
                redisTemplate.delete(CACHE_KEY_PREFIX + walletId);
            }
        } catch (Exception e) {
            log.warn("Non-fatal: Redis cache eviction failed for walletId {}: {}", walletId, e.getMessage());
        }
    }
}
