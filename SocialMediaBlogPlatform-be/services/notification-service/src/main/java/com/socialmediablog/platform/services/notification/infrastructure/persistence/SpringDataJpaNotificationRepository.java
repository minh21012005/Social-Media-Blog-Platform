package com.socialmediablog.platform.services.notification.infrastructure.persistence;

import com.socialmediablog.platform.services.notification.infrastructure.entity.JpaNotificationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaNotificationRepository extends JpaRepository<JpaNotificationEntity, UUID> {

    List<JpaNotificationEntity> findByRecipientId(UUID recipientId);
}
