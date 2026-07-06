package com.socialmediablog.platform.services.comment.infrastructure.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialmediablog.platform.common.events.DomainEvent;
import com.socialmediablog.platform.services.comment.application.port.out.CommentEventPublisher;
import com.socialmediablog.platform.services.comment.infrastructure.entity.JpaOutboxEventEntity;
import com.socialmediablog.platform.services.comment.infrastructure.persistence.SpringDataJpaOutboxEventRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OutboxCommentEventPublisher implements CommentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxCommentEventPublisher.class);

    private final SpringDataJpaOutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxCommentEventPublisher(
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
                    "Comment",
                    event.eventType(),
                    payload,
                    event.occurredAt()
            ));
        } catch (Exception e) {
            log.error("[OutboxCommentEventPublisher] Failed to serialize event type={}: {}", event.eventType(), e.getMessage(), e);
            throw new RuntimeException("Failed to serialize domain event", e);
        }
    }
}

