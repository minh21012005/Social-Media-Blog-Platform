package com.socialmediablog.platform.services.comment.api.dto;

import com.socialmediablog.platform.services.comment.application.result.CommentStatsView;
import java.time.Instant;

public record CommentStatsResponse(
        long clapCount,
        long replyCount,
        Instant lastInteractionAt
) {

    public static CommentStatsResponse from(CommentStatsView stats) {
        return new CommentStatsResponse(
                stats.clapCount(),
                stats.replyCount(),
                stats.lastInteractionAt()
        );
    }
}
