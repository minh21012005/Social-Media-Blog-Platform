package com.socialmediablog.platform.services.article.api.dto;

import com.socialmediablog.platform.services.article.domain.aggregate.Article;
import com.socialmediablog.platform.services.article.domain.model.ArticleStatus;
import java.util.UUID;

public record ArticleCommentPolicyResponse(
        UUID articleId,
        UUID authorId,
        String status,
        boolean commentable
) {

    public static ArticleCommentPolicyResponse from(Article article) {
        return new ArticleCommentPolicyResponse(
                article.id().value(),
                article.authorId().value(),
                article.status().name(),
                article.status() == ArticleStatus.PUBLISHED
        );
    }
}
