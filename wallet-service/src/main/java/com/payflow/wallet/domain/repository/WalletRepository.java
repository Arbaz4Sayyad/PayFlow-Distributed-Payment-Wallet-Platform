package com.payflow.wallet.domain.repository;

import com.payflow.wallet.domain.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdForUpdate(@Param("id") UUID id);

    /**
     * Primary Concurrency Shield: Atomic conditional debit directly at the database level.
     * Guarantees zero double-spending: returns 1 if successfully decremented, 0 if insufficient funds or inactive.
     */
    @Modifying
    @Query(value = """
            UPDATE wallets
            SET balance_minor = balance_minor - :amountMinor,
                version = version + 1,
                updated_at = :now
            WHERE id = :walletId
              AND balance_minor >= :amountMinor
              AND status = 'ACTIVE'
            """, nativeQuery = true)
    int executeAtomicDebit(
            @Param("walletId") UUID walletId,
            @Param("amountMinor") long amountMinor,
            @Param("now") Instant now
    );

    /**
     * Atomic credit directly at the database level.
     * Returns 1 if active wallet credited, 0 if inactive.
     */
    @Modifying
    @Query(value = """
            UPDATE wallets
            SET balance_minor = balance_minor + :amountMinor,
                version = version + 1,
                updated_at = :now
            WHERE id = :walletId
              AND status = 'ACTIVE'
            """, nativeQuery = true)
    int executeAtomicCredit(
            @Param("walletId") UUID walletId,
            @Param("amountMinor") long amountMinor,
            @Param("now") Instant now
    );
}
