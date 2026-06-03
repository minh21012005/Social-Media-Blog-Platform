package com.socialmediablog.platform.services.comment.application.result;

import com.socialmediablog.platform.services.comment.domain.aggregate.Comment;
import java.time.Instant;
import java.util.UUID;

public record CommentView(
        UUID id,
        UUID articleId,
        UUID authorId,
        UUID parentCommentId,
        String content,
        String status,
        Instant editedAt,
        Instant deletedAt,
        Instant createdAt,
        Instant updatedAt,
        CommentStatsView stats
) {

    public static CommentView from(Comment comment, CommentStatsView stats) {
        return new CommentView(
                comment.id().value(),
                comment.articleId().value(),
                comment.authorId().value(),
                comment.parentCommentId() == null ? null : comment.parentCommentId().value(),
                comment.content().value(),
                comment.status().name(),
                comment.editedAt(),
                comment.deletedAt(),
                comment.createdAt(),
                comment.updatedAt(),
                stats
        );
    }
}
