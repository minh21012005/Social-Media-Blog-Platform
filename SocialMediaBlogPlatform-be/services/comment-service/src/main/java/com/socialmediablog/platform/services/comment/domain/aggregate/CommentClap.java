package com.socialmediablog.platform.services.comment.domain.aggregate;

import com.socialmediablog.platform.services.comment.domain.vo.CommentId;
import java.time.Instant;
import java.util.UUID;

public class CommentClap {

    private final UUID id;
    private final CommentId commentId;
    private final UUID userId;
    private final int clapCount;
    private final Instant lastClappedAt;
    private final Instant createdAt;

    private CommentClap(
            UUID id,
            CommentId commentId,
            UUID userId,
            int clapCount,
            Instant lastClappedAt,
            Instant createdAt
    ) {
        this.id = id;
        this.commentId = commentId;
        this.userId = userId;
        if (clapCount < 1) {
            throw new IllegalArgumentException("Comment clap count must be at least 1");
        }
        this.clapCount = clapCount;
        this.lastClappedAt = lastClappedAt;
        this.createdAt = createdAt;
    }

    public static CommentClap create(CommentId commentId, UUID userId, Instant now) {
        return new CommentClap(UUID.randomUUID(), commentId, userId, 1, now, now);
    }

    public static CommentClap restore(
            UUID id,
            UUID commentId,
            UUID userId,
            int clapCount,
            Instant lastClappedAt,
            Instant createdAt
    ) {
        return new CommentClap(id, CommentId.of(commentId), userId, clapCount, lastClappedAt, createdAt);
    }

    public CommentClap clap(Instant now) {
        return new CommentClap(id, commentId, userId, clapCount + 1, now, createdAt);
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

    public int clapCount() {
        return clapCount;
    }

    public Instant lastClappedAt() {
        return lastClappedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
