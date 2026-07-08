package com.socialmediablog.platform.services.notification.infrastructure.feign;

import java.time.Instant;
import java.util.UUID;

/**
 * Mirrors FollowUserItemResponse from follower-service.
 * Only contains the fields we need.
 */
public record FollowerItem(UUID userId, Instant followedAt) {
}
