package com.socialmediablog.platform.services.interaction.infrastructure.adapter;

import com.socialmediablog.platform.common.events.DomainEvent;
import com.socialmediablog.platform.services.interaction.application.port.out.InteractionEventPublisher;
import com.socialmediablog.platform.services.interaction.infrastructure.entity.JpaOutboxEventEntity;
import com.socialmediablog.platform.services.interaction.infrastructure.persistence.SpringDataJpaOutboxEventRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OutboxInteractionEventPublisher implements InteractionEventPublisher {

    private final SpringDataJpaOutboxEventRepository repository;

    public OutboxInteractionEventPublisher(SpringDataJpaOutboxEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public void publish(UUID aggregateId, DomainEvent event) {
        repository.save(JpaOutboxEventEntity.pending(
                event.eventId(),
                aggregateId,
                "Interaction",
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
