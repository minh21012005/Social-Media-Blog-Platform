package com.socialmediablog.platform.services.article.domain.vo;

import java.util.UUID;

public record ArticleMediaAssetId(UUID value) {

    public static ArticleMediaAssetId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Article media asset id is required");
        }
        return new ArticleMediaAssetId(value);
    }
}
