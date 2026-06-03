package com.socialmediablog.platform.services.comment.application.result;

import java.util.UUID;

public record ArticleCommentPolicy(
        UUID articleId,
        UUID authorId,
        String status,
        boolean commentable
) {
}
