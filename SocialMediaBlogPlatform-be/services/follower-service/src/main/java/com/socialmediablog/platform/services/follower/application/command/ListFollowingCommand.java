package com.socialmediablog.platform.services.follower.application.command;

import java.util.UUID;

public record ListFollowingCommand(UUID userId, int page, int size) {
}
