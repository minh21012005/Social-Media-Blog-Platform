package com.socialmediablog.platform.services.comment.infrastructure.adapter;

import com.socialmediablog.platform.common.events.DomainEvent;
import com.socialmediablog.platform.services.comment.application.port.out.CommentEventPublisher;
import com.socialmediablog.platform.services.comment.infrastructure.entity.JpaOutboxEventEntity;
import com.socialmediablog.platform.services.comment.infrastructure.persistence.SpringDataJpaOutboxEventRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OutboxCommentEventPublisher implements CommentEventPublisher {

    private final SpringDataJpaOutboxEventRepository repository;

    public OutboxCommentEventPublisher(SpringDataJpaOutboxEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public void publish(UUID aggregateId, DomainEvent event) {
        repository.save(JpaOutboxEventEntity.pending(
                event.eventId(),
                aggregateId,
                "Comment",
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
