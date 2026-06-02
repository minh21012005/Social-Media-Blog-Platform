package com.socialmediablog.platform.services.interaction.application.command;

import java.util.UUID;

public record BookmarkArticleCommand(
    UUID userId,
    UUID articleId
) {
}
