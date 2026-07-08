package com.socialmediablog.platform.services.interaction.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.socialmediablog.platform.common.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record InteractionRemovedEvent(UUID eventId, UUID interactionId, UUID targetId, UUID userId, Instant occurredAt)
        implements DomainEvent {

    @Override
    @JsonProperty("eventType")
    public String eventType() {
        return "interaction.removed";
    }
}
