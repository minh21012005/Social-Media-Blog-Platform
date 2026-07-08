package com.socialmediablog.platform.services.interaction.application.command;

import java.util.UUID;

public record ClapArticleCommand(UUID userId, UUID articleId) {
}
