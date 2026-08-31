package com.payflow.merchant.domain.repository;

import com.payflow.merchant.domain.entity.Merchant;
import com.payflow.merchant.domain.enums.MerchantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    boolean existsByUserId(UUID userId);

    Optional<Merchant> findByUserId(UUID userId);

    Optional<Merchant> findByIdAndStatus(UUID id, MerchantStatus status);
}
