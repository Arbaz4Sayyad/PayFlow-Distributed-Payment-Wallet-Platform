package com.payflow.fraud.domain.repository;

import com.payflow.fraud.domain.entity.BlacklistRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlacklistRecordRepository extends JpaRepository<BlacklistRecord, UUID> {

    boolean existsByTargetTypeAndTargetValue(String targetType, String targetValue);

    Optional<BlacklistRecord> findByTargetTypeAndTargetValue(String targetType, String targetValue);
}
