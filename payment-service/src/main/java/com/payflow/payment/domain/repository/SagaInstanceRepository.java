package com.payflow.payment.domain.repository;

import com.payflow.common.model.enums.SagaStatus;
import com.payflow.payment.domain.entity.SagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SagaInstanceRepository extends JpaRepository<SagaInstance, UUID> {

    Optional<SagaInstance> findByCorrelationId(String correlationId);

    List<SagaInstance> findByStatus(SagaStatus status);
}
