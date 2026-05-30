package com.socialmediablog.platform.services.article.application.command;

import java.util.UUID;

public record RecordArticleViewCommand(
        UUID articleId,
        UUID viewerId,
        String anonymousViewerKey,
        String source
) {
}
