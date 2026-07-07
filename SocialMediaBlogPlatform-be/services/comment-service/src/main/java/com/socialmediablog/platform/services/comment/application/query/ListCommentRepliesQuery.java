package com.socialmediablog.platform.services.comment.application.query;

import java.util.UUID;

public record ListCommentRepliesQuery(UUID commentId, int page, int size, UUID currentUserId) {

    public ListCommentRepliesQuery(UUID commentId) {
        this(commentId, 0, 20, null);
    }
}
