package com.socialmediablog.platform.services.interaction.domain.event;

import com.socialmediablog.platform.common.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record InteractionRecordedEvent(UUID eventId, UUID interactionId, UUID targetId, UUID userId, Instant occurredAt)
        implements DomainEvent {

    @Override
    public String eventType() {
        return "interaction.recorded";
    }
}
