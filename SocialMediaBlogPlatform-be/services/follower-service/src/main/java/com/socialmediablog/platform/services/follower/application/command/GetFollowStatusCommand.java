package com.socialmediablog.platform.services.follower.application.command;

import java.util.UUID;

public record GetFollowStatusCommand(UUID viewerId, UUID targetUserId) {
}
