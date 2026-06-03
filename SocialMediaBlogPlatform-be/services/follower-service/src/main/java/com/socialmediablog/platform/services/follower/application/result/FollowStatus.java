package com.socialmediablog.platform.services.follower.application.result;

import java.util.UUID;

public record FollowStatus(UUID viewerId, UUID targetUserId, boolean following) {
}
