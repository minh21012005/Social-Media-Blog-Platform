package com.socialmediablog.platform.services.user.application.command;

import java.util.UUID;

public record LogoutCommand(UUID userId, String refreshToken) {
}
