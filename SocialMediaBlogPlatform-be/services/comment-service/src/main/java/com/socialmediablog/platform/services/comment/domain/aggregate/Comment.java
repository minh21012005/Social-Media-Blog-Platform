package com.socialmediablog.platform.services.comment.domain.aggregate;

import com.socialmediablog.platform.services.comment.domain.vo.CommentId;
import java.util.UUID;

public class Comment {

    private final CommentId id;

    private Comment(CommentId id) {
        this.id = id;
    }

    public static Comment restore(UUID id) {
        return new Comment(CommentId.of(id));
    }

    public CommentId id() {
        return id;
    }
}
