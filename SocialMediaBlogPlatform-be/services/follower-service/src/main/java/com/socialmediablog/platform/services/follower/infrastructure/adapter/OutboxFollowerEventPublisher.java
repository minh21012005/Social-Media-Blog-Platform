package com.socialmediablog.platform.services.follower.infrastructure.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialmediablog.platform.common.events.DomainEvent;
import com.socialmediablog.platform.services.follower.application.port.out.FollowerEventPublisher;
import com.socialmediablog.platform.services.follower.infrastructure.entity.JpaOutboxEventEntity;
import com.socialmediablog.platform.services.follower.infrastructure.persistence.SpringDataJpaOutboxEventRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OutboxFollowerEventPublisher implements FollowerEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxFollowerEventPublisher.class);

    private final SpringDataJpaOutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxFollowerEventPublisher(
            SpringDataJpaOutboxEventRepository repository,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(UUID aggregateId, DomainEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            repository.save(JpaOutboxEventEntity.pending(
                    event.eventId(),
                    aggregateId,
                    "FollowRelation",
                    event.eventType(),
                    payload,
                    event.occurredAt()
            ));
        } catch (Exception e) {
            log.error("[OutboxFollowerEventPublisher] Failed to serialize event type={}: {}", event.eventType(), e.getMessage(), e);
            throw new RuntimeException("Failed to serialize domain event", e);
        }
    }
}

