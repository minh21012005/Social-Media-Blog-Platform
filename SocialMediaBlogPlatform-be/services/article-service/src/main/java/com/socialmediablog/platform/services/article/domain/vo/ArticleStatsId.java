package com.socialmediablog.platform.services.article.domain.vo;

import java.util.UUID;

public record ArticleStatsId(UUID value) {

    public static ArticleStatsId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Article stats id is required");
        }
        return new ArticleStatsId(value);
    }
}
