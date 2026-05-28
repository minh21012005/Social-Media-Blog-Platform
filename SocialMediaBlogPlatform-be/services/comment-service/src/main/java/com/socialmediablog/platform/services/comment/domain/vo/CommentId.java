package com.socialmediablog.platform.services.comment.domain.vo;

import java.util.UUID;

public record CommentId(UUID value) {

    public static CommentId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Comment id is required");
        }
        return new CommentId(value);
    }
}
