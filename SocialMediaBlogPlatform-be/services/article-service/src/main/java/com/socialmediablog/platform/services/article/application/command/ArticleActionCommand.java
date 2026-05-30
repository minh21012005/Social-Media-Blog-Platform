package com.socialmediablog.platform.services.article.application.command;

import java.util.UUID;

public record ArticleActionCommand(UUID articleId, UUID actorId) {
}
