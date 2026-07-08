package com.socialmediablog.platform.services.follower.application.command;

import java.util.UUID;

public record UnblockUserCommand(UUID blockerId, UUID blockedUserId) {
}
