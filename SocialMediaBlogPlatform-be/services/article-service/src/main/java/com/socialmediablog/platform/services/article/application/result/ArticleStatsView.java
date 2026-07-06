package com.socialmediablog.platform.services.article.application.result;

import com.socialmediablog.platform.services.article.domain.aggregate.ArticleStats;

public record ArticleStatsView(
        long clapCount,
        long likeCount,
        long commentCount,
        long viewCount,
        long bookmarkCount
) {

    public static ArticleStatsView empty() {
        return new ArticleStatsView(0, 0, 0, 0, 0);
    }

    public static ArticleStatsView from(ArticleStats stats) {
        if (stats == null) {
            return empty();
        }
        // The likeCount is handled separately in the ArticleApplicationService
        return new ArticleStatsView(stats.clapCount(), 0, stats.commentCount(), stats.viewCount(), stats.bookmarkCount());
    }
}
