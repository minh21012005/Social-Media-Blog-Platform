package com.socialmediablog.platform.services.article.application.command;

import java.util.UUID;

public record ListMyArticlesCommand(UUID authorId, String status, int page, int size) {
}
