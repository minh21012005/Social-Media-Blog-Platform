package com.socialmediablog.platform.services.follower.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.socialmediablog.platform.common.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record UserFollowAcceptedEvent(UUID eventId, UUID followerId, UUID followedUserId, Instant occurredAt)
        implements DomainEvent {

    @Override
    @JsonProperty("eventType")
    public String eventType() {
        return "user.follow-accepted";
    }
}
