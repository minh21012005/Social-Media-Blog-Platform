package com.socialmediablog.platform.services.comment.infrastructure.client;

import com.socialmediablog.platform.services.comment.application.result.ArticleCommentPolicy;
import java.util.UUID;

public record ArticleCommentPolicyResponse(
        UUID articleId,
        UUID authorId,
        String status,
        boolean commentable
) {

    public ArticleCommentPolicy toApplication() {
        return new ArticleCommentPolicy(articleId, authorId, status, commentable);
    }
}
