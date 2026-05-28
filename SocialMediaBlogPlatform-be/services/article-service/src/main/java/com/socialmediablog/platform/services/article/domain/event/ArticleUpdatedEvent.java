package com.socialmediablog.platform.services.article.domain.event;

import com.socialmediablog.platform.common.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record ArticleUpdatedEvent(UUID eventId, UUID articleId, UUID authorId, Instant occurredAt)
        implements DomainEvent {

    @Override
    public String eventType() {
        return "article.updated";
    }
}
