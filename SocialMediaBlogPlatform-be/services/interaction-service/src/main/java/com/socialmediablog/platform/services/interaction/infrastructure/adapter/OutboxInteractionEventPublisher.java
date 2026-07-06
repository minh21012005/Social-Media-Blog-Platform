package com.socialmediablog.platform.services.interaction.infrastructure.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialmediablog.platform.common.events.DomainEvent;
import com.socialmediablog.platform.services.interaction.application.port.out.InteractionEventPublisher;
import com.socialmediablog.platform.services.interaction.infrastructure.entity.JpaOutboxEventEntity;
import com.socialmediablog.platform.services.interaction.infrastructure.persistence.SpringDataJpaOutboxEventRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OutboxInteractionEventPublisher implements InteractionEventPublisher {

    private final SpringDataJpaOutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxInteractionEventPublisher(
            SpringDataJpaOutboxEventRepository repository,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
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
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to serialize interaction event payload for aggregateId=" + aggregateId, exception);
        }
    }
}
