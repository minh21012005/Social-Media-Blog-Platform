package com.socialmediablog.platform.services.follower.application.command;

import java.util.UUID;

public record BlockUserCommand(UUID blockerId, UUID blockedUserId) {
}
