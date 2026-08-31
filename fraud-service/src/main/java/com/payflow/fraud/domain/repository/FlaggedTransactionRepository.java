package com.payflow.fraud.domain.repository;

import com.payflow.fraud.domain.entity.FlaggedTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FlaggedTransactionRepository extends JpaRepository<FlaggedTransaction, UUID> {

    Optional<FlaggedTransaction> findByTransactionId(UUID transactionId);

    List<FlaggedTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
