package com.socialmediablog.platform.services.interaction.infrastructure.adapter;

import com.socialmediablog.platform.common.events.DomainEvent;
import com.socialmediablog.platform.services.interaction.application.port.out.InteractionEventPublisher;
import com.socialmediablog.platform.services.interaction.infrastructure.entity.JpaOutboxEventEntity;
import com.socialmediablog.platform.services.interaction.infrastructure.persistence.SpringDataJpaOutboxEventRepository;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class OutboxInteractionEventPublisher implements InteractionEventPublisher {

    private final SpringDataJpaOutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxInteractionEventPublisher(SpringDataJpaOutboxEventRepository repository, ObjectMapper objectMapper) {
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
                payload(event),
                event.occurredAt()
        ));
    }

    private String payload(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize interaction event payload", e);
        }
    }
}
