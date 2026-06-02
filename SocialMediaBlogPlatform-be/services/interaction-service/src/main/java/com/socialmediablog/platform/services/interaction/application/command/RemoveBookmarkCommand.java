package com.socialmediablog.platform.services.interaction.application.command;

import java.util.UUID;

public record RemoveBookmarkCommand(
    UUID userId,
    UUID articleId
) {
}
