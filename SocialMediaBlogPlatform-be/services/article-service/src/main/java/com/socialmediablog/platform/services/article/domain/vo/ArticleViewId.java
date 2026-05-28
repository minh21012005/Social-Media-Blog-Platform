package com.socialmediablog.platform.services.article.domain.vo;

import java.util.UUID;

public record ArticleViewId(UUID value) {

    public static ArticleViewId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Article view id is required");
        }
        return new ArticleViewId(value);
    }
}
