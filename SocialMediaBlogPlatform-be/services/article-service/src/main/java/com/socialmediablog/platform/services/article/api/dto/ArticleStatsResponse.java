package com.socialmediablog.platform.services.article.api.dto;

import com.socialmediablog.platform.services.article.application.result.ArticleStatsView;

public record ArticleStatsResponse(
        long clapCount,
        long likeCount,
        long commentCount,
        long viewCount,
        long bookmarkCount
) {

    public static ArticleStatsResponse from(ArticleStatsView stats) {
        return new ArticleStatsResponse(
                stats.clapCount(),
                stats.likeCount(),
                stats.commentCount(),
                stats.viewCount(),
                stats.bookmarkCount()
        );
    }
}
