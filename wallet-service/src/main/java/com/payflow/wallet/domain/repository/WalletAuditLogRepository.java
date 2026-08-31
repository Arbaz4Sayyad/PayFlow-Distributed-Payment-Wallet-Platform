package com.payflow.wallet.domain.repository;

import com.payflow.wallet.domain.entity.WalletAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WalletAuditLogRepository extends JpaRepository<WalletAuditLog, UUID> {

    List<WalletAuditLog> findByWalletIdOrderByCreatedAtDesc(UUID walletId);
}
