package com.socialmediablog.platform.services.notification.domain.event;

import com.socialmediablog.platform.common.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record NotificationCreatedEvent(
        UUID eventId,
        UUID notificationId,
        UUID recipientId,
        String type,
        Instant occurredAt
) implements DomainEvent {

    public NotificationCreatedEvent(UUID notificationId, UUID recipientId, String type, Instant occurredAt) {
        this(UUID.randomUUID(), notificationId, recipientId, type, occurredAt);
    }

    @Override
    public String eventType() {
        return "notification.created";
    }
}
