package com.socialmediablog.platform.services.article.infrastructure.adapter;

import com.socialmediablog.platform.common.events.DomainEvent;
import com.socialmediablog.platform.services.article.application.port.out.ArticleEventPublisher;
import com.socialmediablog.platform.services.article.infrastructure.entity.JpaOutboxEventEntity;
import com.socialmediablog.platform.services.article.infrastructure.persistence.SpringDataJpaOutboxEventRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OutboxArticleEventPublisher implements ArticleEventPublisher {

    private final SpringDataJpaOutboxEventRepository repository;

    public OutboxArticleEventPublisher(SpringDataJpaOutboxEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public void publish(UUID aggregateId, DomainEvent event) {
        repository.save(JpaOutboxEventEntity.pending(
                event.eventId(),
                aggregateId,
                "Article",
                event.eventType(),
                payload(aggregateId, event),
                event.occurredAt()
        ));
    }

    private String payload(UUID aggregateId, DomainEvent event) {
        return """
                {"eventId":"%s","aggregateId":"%s","eventType":"%s","occurredAt":"%s"}
                """.formatted(event.eventId(), aggregateId, event.eventType(), event.occurredAt()).trim();
    }
}
