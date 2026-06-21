package com.socialmediablog.platform.services.notification.infrastructure.adapter;

import com.socialmediablog.platform.services.notification.domain.aggregate.Notification;
import com.socialmediablog.platform.services.notification.domain.repository.NotificationRepository;
import com.socialmediablog.platform.services.notification.domain.vo.NotificationId;
import com.socialmediablog.platform.services.notification.domain.vo.RecipientId;
import com.socialmediablog.platform.services.notification.infrastructure.entity.JpaNotificationEntity;
import com.socialmediablog.platform.services.notification.infrastructure.persistence.SpringDataJpaNotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialmediablog.platform.services.notification.infrastructure.entity.JpaOutboxEventEntity;
import com.socialmediablog.platform.services.notification.infrastructure.messaging.NotificationPushEvent;
import com.socialmediablog.platform.services.notification.infrastructure.persistence.SpringDataJpaOutboxEventRepository;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaNotificationRepositoryAdapter implements NotificationRepository {

    private final SpringDataJpaNotificationRepository repository;
    private final SpringDataJpaOutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public JpaNotificationRepositoryAdapter(
            SpringDataJpaNotificationRepository repository,
            SpringDataJpaOutboxEventRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
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
    @Transactional
    public Notification save(Notification notification) {
        Notification saved = repository.save(JpaNotificationEntity.fromDomain(notification)).toDomain();
        
        try {
            NotificationPushEvent pushEvent = new NotificationPushEvent(
                    saved.id().value(),
                    saved.actorId(),
                    saved.recipientId().value(),
                    saved.type().name(),
                    saved.subjectType(),
                    saved.subjectId(),
                    saved.title(),
                    saved.body(),
                    saved.status().name(),
                    saved.createdAt()
            );
            String payload = objectMapper.writeValueAsString(pushEvent);
            JpaOutboxEventEntity outboxEvent = JpaOutboxEventEntity.pending(
                    UUID.randomUUID(),
                    saved.id().value(),
                    "Notification",
                    "notification.events",
                    payload,
                    Instant.now()
            );
            outboxRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize outbox event payload", e);
        }
        
        return saved;
    }
}
