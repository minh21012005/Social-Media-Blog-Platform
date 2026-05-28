package com.socialmediablog.platform.services.notification.infrastructure.adapter;

import com.socialmediablog.platform.services.notification.domain.aggregate.Notification;
import com.socialmediablog.platform.services.notification.domain.repository.NotificationRepository;
import com.socialmediablog.platform.services.notification.domain.vo.NotificationId;
import com.socialmediablog.platform.services.notification.domain.vo.RecipientId;
import com.socialmediablog.platform.services.notification.infrastructure.entity.JpaNotificationEntity;
import com.socialmediablog.platform.services.notification.infrastructure.persistence.SpringDataJpaNotificationRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaNotificationRepositoryAdapter implements NotificationRepository {

    private final SpringDataJpaNotificationRepository repository;

    public JpaNotificationRepositoryAdapter(SpringDataJpaNotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Notification> findById(NotificationId id) {
        return repository.findById(id.value()).map(JpaNotificationEntity::toDomain);
    }

    @Override
    public List<Notification> findByRecipientId(RecipientId recipientId) {
        return repository.findByRecipientId(recipientId.value()).stream()
                .map(JpaNotificationEntity::toDomain)
                .toList();
    }

    @Override
    public Notification save(Notification notification) {
        return repository.save(JpaNotificationEntity.fromDomain(notification)).toDomain();
    }
}
