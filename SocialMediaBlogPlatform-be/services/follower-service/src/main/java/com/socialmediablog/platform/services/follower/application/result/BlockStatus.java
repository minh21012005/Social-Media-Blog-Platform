package com.socialmediablog.platform.services.follower.application.result;

import java.util.UUID;

public record BlockStatus(UUID viewerId, UUID targetUserId, boolean blocked) {
}
