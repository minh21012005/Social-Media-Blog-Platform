package com.socialmediablog.platform.services.user.application.command;

import java.util.UUID;

public record UpdateUserProfileCommand(UUID userId, String displayName, String bio, String avatarUrl) {
}
