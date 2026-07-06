package com.socialmediablog.platform.services.comment.api.dto;

import com.socialmediablog.platform.services.comment.application.result.CommentView;
import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
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
        Instant pinnedAt,
        CommentStatsResponse stats) {

    public static CommentResponse from(CommentView comment) {
        return new CommentResponse(
                comment.id(),
                comment.articleId(),
                comment.authorId(),
                comment.parentCommentId(),
                comment.content(),
                comment.status(),
                comment.editedAt(),
                comment.deletedAt(),
                comment.createdAt(),
                comment.updatedAt(),
                comment.pinnedAt(),
                CommentStatsResponse.from(comment.stats()));
    }
}
