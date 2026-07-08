package com.socialmediablog.platform.services.comment.domain.aggregate;

import com.socialmediablog.platform.services.comment.domain.vo.CommentId;
import java.time.Instant;
import java.util.UUID;

public class CommentClap {

    private final UUID id;
    private final CommentId commentId;
    private final UUID userId;
    private final Instant createdAt;

    private CommentClap(UUID id, CommentId commentId, UUID userId, Instant createdAt) {
        this.id = id;
        this.commentId = commentId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public static CommentClap create(CommentId commentId, UUID userId, Instant now) {
        return new CommentClap(UUID.randomUUID(), commentId, userId, now);
    }

    public static CommentClap restore(UUID id, UUID commentId, UUID userId, Instant createdAt) {
        return new CommentClap(id, CommentId.of(commentId), userId, createdAt);
    }

    public UUID id() {
        return id;
    }

    public CommentId commentId() {
        return commentId;
    }

    public UUID userId() {
        return userId;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
