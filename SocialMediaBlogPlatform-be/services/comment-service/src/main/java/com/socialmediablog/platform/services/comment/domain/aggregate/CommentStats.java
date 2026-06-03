package com.socialmediablog.platform.services.comment.domain.aggregate;

import com.socialmediablog.platform.services.comment.domain.vo.CommentId;
import com.socialmediablog.platform.services.comment.domain.vo.CommentStatsId;
import java.time.Instant;
import java.util.UUID;

public class CommentStats {

    private final CommentStatsId id;
    private final CommentId commentId;
    private final long clapCount;
    private final long replyCount;
    private final Instant lastInteractionAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    private CommentStats(
            CommentStatsId id,
            CommentId commentId,
            long clapCount,
            long replyCount,
            Instant lastInteractionAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.commentId = commentId;
        this.clapCount = nonNegative(clapCount, "clap count");
        this.replyCount = nonNegative(replyCount, "reply count");
        this.lastInteractionAt = lastInteractionAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CommentStats empty(CommentId commentId, Instant now) {
        return new CommentStats(CommentStatsId.of(UUID.randomUUID()), commentId, 0, 0, null, now, now);
    }

    public static CommentStats restore(
            UUID id,
            UUID commentId,
            long clapCount,
            long replyCount,
            Instant lastInteractionAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new CommentStats(
                CommentStatsId.of(id),
                CommentId.of(commentId),
                clapCount,
                replyCount,
                lastInteractionAt,
                createdAt,
                updatedAt
        );
    }

    private static long nonNegative(long value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException("Comment " + field + " must not be negative");
        }
        return value;
    }

    public CommentStats incrementReplyCount(Instant now) {
        return new CommentStats(
                id,
                commentId,
                clapCount,
                replyCount + 1,
                now,
                createdAt,
                now
        );
    }

    public CommentStats decrementReplyCount(Instant now) {
        return new CommentStats(
                id,
                commentId,
                clapCount,
                Math.max(0, replyCount - 1),
                now,
                createdAt,
                now
        );
    }

    public CommentStatsId id() {
        return id;
    }

    public CommentId commentId() {
        return commentId;
    }

    public long clapCount() {
        return clapCount;
    }

    public long replyCount() {
        return replyCount;
    }

    public Instant lastInteractionAt() {
        return lastInteractionAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
