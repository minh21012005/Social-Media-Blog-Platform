package com.socialmediablog.platform.services.notification.infrastructure.adapter;

import com.socialmediablog.platform.services.notification.domain.aggregate.NotificationPreference;
import com.socialmediablog.platform.services.notification.domain.repository.NotificationPreferenceRepository;
import com.socialmediablog.platform.services.notification.infrastructure.entity.JpaNotificationPreferenceEntity;
import com.socialmediablog.platform.services.notification.infrastructure.persistence.SpringDataJpaNotificationPreferenceRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaNotificationPreferenceRepositoryAdapter implements NotificationPreferenceRepository {

    private final SpringDataJpaNotificationPreferenceRepository repository;

    public JpaNotificationPreferenceRepositoryAdapter(SpringDataJpaNotificationPreferenceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<NotificationPreference> findByUserId(UUID userId) {
        return repository.findByUserId(userId).map(JpaNotificationPreferenceEntity::toDomain);
    }

    @Override
    public NotificationPreference save(NotificationPreference preference) {
        return repository.save(JpaNotificationPreferenceEntity.fromDomain(preference)).toDomain();
    }
}
