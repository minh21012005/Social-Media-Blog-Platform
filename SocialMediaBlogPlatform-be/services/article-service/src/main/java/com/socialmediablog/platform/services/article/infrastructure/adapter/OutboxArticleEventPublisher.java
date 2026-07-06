package com.socialmediablog.platform.services.article.infrastructure.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialmediablog.platform.common.events.DomainEvent;
import com.socialmediablog.platform.services.article.application.port.out.ArticleEventPublisher;
import com.socialmediablog.platform.services.article.infrastructure.entity.JpaOutboxEventEntity;
import com.socialmediablog.platform.services.article.infrastructure.persistence.SpringDataJpaOutboxEventRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OutboxArticleEventPublisher implements ArticleEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxArticleEventPublisher.class);

    private final SpringDataJpaOutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxArticleEventPublisher(
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
                    "Article",
                    event.eventType(),
                    payload,
                    event.occurredAt()
            ));
        } catch (Exception e) {
            log.error("[OutboxArticleEventPublisher] Failed to serialize event type={}: {}", event.eventType(), e.getMessage(), e);
            throw new RuntimeException("Failed to serialize domain event", e);
        }
    }
}

