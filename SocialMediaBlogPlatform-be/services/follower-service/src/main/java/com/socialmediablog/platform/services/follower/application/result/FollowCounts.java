package com.socialmediablog.platform.services.follower.application.result;

import java.util.UUID;

public record FollowCounts(UUID userId, long followers, long following) {
}
