package com.socialmediablog.platform.services.notification.infrastructure.persistence;

import com.socialmediablog.platform.services.notification.infrastructure.entity.JpaNotificationPreferenceEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaNotificationPreferenceRepository extends JpaRepository<JpaNotificationPreferenceEntity, UUID> {

    Optional<JpaNotificationPreferenceEntity> findByUserId(UUID userId);
}
