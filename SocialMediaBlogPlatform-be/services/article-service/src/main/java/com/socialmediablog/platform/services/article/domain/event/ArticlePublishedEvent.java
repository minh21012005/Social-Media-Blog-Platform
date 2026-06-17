package com.socialmediablog.platform.services.article.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.socialmediablog.platform.common.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record ArticlePublishedEvent(UUID eventId, UUID articleId, UUID authorId, Instant occurredAt)
        implements DomainEvent {

    @Override
    @JsonProperty("eventType")
    public String eventType() {
        return "article.published";
    }
}
