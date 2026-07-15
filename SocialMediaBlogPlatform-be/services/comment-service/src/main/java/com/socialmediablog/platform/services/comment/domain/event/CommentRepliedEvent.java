package com.socialmediablog.platform.services.comment.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.socialmediablog.platform.common.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record CommentRepliedEvent(
        UUID eventId,
        UUID replyId,
        UUID parentCommentId,
        UUID articleId,
        UUID authorId,
        UUID parentAuthorId,
        Instant occurredAt
) implements DomainEvent {

    @Override
    @JsonProperty("eventType")
    public String eventType() {
        return "comment.replied";
    }
}