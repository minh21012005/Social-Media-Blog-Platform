package com.socialmediablog.platform.services.follower.application.result;

import java.time.Instant;
import java.util.UUID;

public record FollowUserItem(UUID userId, Instant followedAt) {
}
