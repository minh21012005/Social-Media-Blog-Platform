package com.socialmediablog.platform.services.interaction.application.command;

import java.util.UUID;

public record LikeArticleCommand(UUID userId, UUID articleId) {
}
