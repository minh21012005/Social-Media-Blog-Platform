package com.socialmediablog.platform.services.follower.application.command;

import java.util.UUID;

public record ListFollowersCommand(UUID userId, int page, int size) {
}
