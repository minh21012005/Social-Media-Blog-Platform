package com.socialmediablog.platform.services.user.domain.event;

import com.socialmediablog.platform.common.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record UserRegisteredEvent(
        UUID eventId,
        UUID userId,
        String username,
        String email,
        Instant occurredAt
) implements DomainEvent {

    @Override
    public String eventType() {
        return "user.registered";
    }
}
