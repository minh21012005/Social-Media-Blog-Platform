package com.socialmediablog.platform.services.comment.application.result;

import com.socialmediablog.platform.services.comment.domain.aggregate.CommentStats;
import java.time.Instant;

public record CommentStatsView(
        long clapCount,
        long replyCount,
        Instant lastInteractionAt
) {

    public static CommentStatsView empty() {
        return new CommentStatsView(0, 0, null);
    }

    public static CommentStatsView from(CommentStats stats) {
        if (stats == null) {
            return empty();
        }
        return new CommentStatsView(stats.clapCount(), stats.replyCount(), stats.lastInteractionAt());
    }
}
