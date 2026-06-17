package com.socialmediablog.platform.services.comment.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.socialmediablog.platform.common.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record CommentDeletedEvent(UUID eventId, UUID commentId, UUID articleId, UUID authorId, Instant occurredAt)
        implements DomainEvent {

    @Override
    @JsonProperty("eventType")
    public String eventType() {
        return "comment.deleted";
    }
}
