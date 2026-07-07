package com.socialmediablog.platform.services.comment.application.query;

import java.util.UUID;

public record ListArticleCommentsQuery(UUID articleId, int page, int size, CommentSortBy sortBy, UUID currentUserId) {

    public ListArticleCommentsQuery(UUID articleId) {
        this(articleId, 0, 20, CommentSortBy.NEWEST, null);
    }
}
