package com.socialmediablog.platform.services.follower.domain.event;

import com.socialmediablog.platform.common.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record UserFollowedEvent(UUID eventId, UUID followerId, UUID followedUserId, Instant occurredAt)
        implements DomainEvent {

    @Override
    public String eventType() {
        return "user.followed";
    }
}
