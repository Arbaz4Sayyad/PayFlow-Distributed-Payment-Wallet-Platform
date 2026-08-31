package com.payflow.notification.domain.repository;

import com.payflow.notification.domain.entity.NotificationLog;
import com.payflow.notification.domain.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    List<NotificationLog> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<NotificationLog> findByStatus(NotificationStatus status);
}
