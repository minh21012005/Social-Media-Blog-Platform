package com.socialmediablog.platform.services.notification.domain.event;

import com.socialmediablog.platform.common.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record NotificationReadEvent(
        UUID eventId,
        UUID notificationId,
        UUID recipientId,
        Instant occurredAt
) implements DomainEvent {

    public NotificationReadEvent(UUID notificationId, UUID recipientId, Instant occurredAt) {
        this(UUID.randomUUID(), notificationId, recipientId, occurredAt);
    }

    @Override
    public String eventType() {
        return "notification.read";
    }
}
