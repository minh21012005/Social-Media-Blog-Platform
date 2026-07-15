package com.socialmediablog.platform.services.comment.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.socialmediablog.platform.common.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record CommentClappedEvent(
        UUID eventId,
        UUID commentId,
        UUID articleId,
        UUID commentAuthorId,
        UUID userId,
        UUID parentCommentId,
        Instant occurredAt
) implements DomainEvent {

    public static CommentClappedEvent create(
            UUID commentId,
            UUID articleId,
            UUID commentAuthorId,
            UUID userId,
            UUID parentCommentId,
            Instant occurredAt
    ) {
        return new CommentClappedEvent(
                UUID.randomUUID(), commentId, articleId, commentAuthorId, userId, parentCommentId, occurredAt);
    }

    @Override
    @JsonProperty("eventType")
    public String eventType() {
        return "comment.clapped";
    }
}