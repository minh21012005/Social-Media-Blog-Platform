package com.socialmediablog.platform.services.article.application.command;

import java.util.Set;
import java.util.UUID;

public record UpdateArticleCommand(
        UUID articleId,
        UUID actorId,
        String title,
        String slug,
        String category,
        String summary,
        String content,
        String coverImageUrl,
        Set<String> tags
) {
}
