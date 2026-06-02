package com.socialmediablog.platform.services.article.application.command;

import java.util.UUID;

public record CurateArticleCommand(
        UUID articleId,
        Integer featuredRank,
        Integer editorPickRank
) {
}
