package com.socialmediablog.platform.services.notification.infrastructure.feign;

import java.util.List;
import java.util.UUID;

/**
 * Mirrors FollowUserPageResponse from follower-service.
 */
public record FollowerPage(
        UUID userId,
        List<FollowerItem> users,
        int page,
        int size,
        long total
) {
}
