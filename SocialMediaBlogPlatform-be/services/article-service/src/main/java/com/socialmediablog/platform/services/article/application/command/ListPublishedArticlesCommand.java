package com.socialmediablog.platform.services.article.application.command;

import java.util.UUID;

public record ListPublishedArticlesCommand(
        String category,
        UUID authorId,
        String tag,
        String query,
        int page,
        int size
) {
}
