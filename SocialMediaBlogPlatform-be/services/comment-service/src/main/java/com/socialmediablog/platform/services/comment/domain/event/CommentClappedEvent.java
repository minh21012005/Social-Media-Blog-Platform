package com.socialmediablog.platform.services.comment.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.socialmediablog.platform.common.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record CommentClappedEvent(UUID eventId, UUID commentId, UUID userId, Instant occurredAt)
        implements DomainEvent {

    public static CommentClappedEvent create(UUID commentId, UUID userId, Instant occurredAt) {
        return new CommentClappedEvent(UUID.randomUUID(), commentId, userId, occurredAt);
    }

    @Override
    @JsonProperty("eventType")
    public String eventType() {
        return "comment.clapped";
    }
}
