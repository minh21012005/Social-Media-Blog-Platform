package com.socialmediablog.platform.services.comment.domain.vo;

import java.util.UUID;

public record CommentStatsId(UUID value) {

    public static CommentStatsId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Comment stats id is required");
        }
        return new CommentStatsId(value);
    }
}
