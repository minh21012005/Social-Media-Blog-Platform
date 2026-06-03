package com.socialmediablog.platform.services.interaction.application.command;

import java.util.UUID;

public record UnlikeArticleCommand(UUID userId, UUID articleId) {
}
