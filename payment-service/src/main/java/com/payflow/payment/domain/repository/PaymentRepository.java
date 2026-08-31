package com.payflow.payment.domain.repository;

import com.payflow.payment.domain.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findBySenderWalletIdAndIdempotencyKey(UUID senderWalletId, String idempotencyKey);

    Page<Payment> findBySenderWalletIdOrRecipientWalletIdOrderByCreatedAtDesc(
            UUID senderWalletId,
            UUID recipientWalletId,
            Pageable pageable
    );
}
