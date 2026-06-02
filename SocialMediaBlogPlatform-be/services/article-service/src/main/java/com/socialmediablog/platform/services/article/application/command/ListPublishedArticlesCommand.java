package com.socialmediablog.platform.services.article.application.command;

import java.util.UUID;

public record ListPublishedArticlesCommand(
        String category,
        UUID authorId,
        String tag,
        String query,
        String sort,
        int page,
        int size
) {
}
