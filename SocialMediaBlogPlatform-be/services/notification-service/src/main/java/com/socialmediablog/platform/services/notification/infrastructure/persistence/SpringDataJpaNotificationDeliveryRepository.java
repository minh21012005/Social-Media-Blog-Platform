package com.socialmediablog.platform.services.notification.infrastructure.persistence;

import com.socialmediablog.platform.services.notification.infrastructure.entity.JpaNotificationDeliveryEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaNotificationDeliveryRepository extends JpaRepository<JpaNotificationDeliveryEntity, UUID> {

    List<JpaNotificationDeliveryEntity> findByNotificationId(UUID notificationId);
}
