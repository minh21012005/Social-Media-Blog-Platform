package com.socialmediablog.platform.services.comment.domain.aggregate;

import com.socialmediablog.platform.services.comment.domain.model.CommentStatus;
import com.socialmediablog.platform.services.comment.domain.vo.ArticleId;
import com.socialmediablog.platform.services.comment.domain.vo.AuthorId;
import com.socialmediablog.platform.services.comment.domain.vo.CommentId;
import com.socialmediablog.platform.services.comment.domain.vo.CommentContent;
import java.time.Instant;
import java.util.UUID;

public class Comment {

    private final CommentId id;
    private final ArticleId articleId;
    private final AuthorId authorId;
    private final CommentId parentCommentId;
    private final CommentContent content;
    private final CommentStatus status;
    private final Instant editedAt;
    private final Instant deletedAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Comment(
            CommentId id,
            ArticleId articleId,
            AuthorId authorId,
            CommentId parentCommentId,
            CommentContent content,
            CommentStatus status,
            Instant editedAt,
            Instant deletedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.articleId = articleId;
        this.authorId = authorId;
        this.parentCommentId = parentCommentId;
        this.content = content;
        this.status = status;
        this.editedAt = editedAt;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Comment create(
            ArticleId articleId,
            AuthorId authorId,
            CommentId parentCommentId,
            CommentContent content,
            Instant now
    ) {
        return new Comment(
                CommentId.of(UUID.randomUUID()),
                articleId,
                authorId,
                parentCommentId,
                content,
                CommentStatus.ACTIVE,
                null,
                null,
                now,
                now
        );
    }

    public static Comment restore(
            UUID id,
            UUID articleId,
            UUID authorId,
            UUID parentCommentId,
            String content,
            CommentStatus status,
            Instant editedAt,
            Instant deletedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Comment(
                CommentId.of(id),
                ArticleId.of(articleId),
                AuthorId.of(authorId),
                parentCommentId == null ? null : CommentId.of(parentCommentId),
                CommentContent.of(content),
                status,
                editedAt,
                deletedAt,
                createdAt,
                updatedAt
        );
    }

    public boolean isReply() {
        return parentCommentId != null;
    }

    public CommentId id() {
        return id;
    }

    public ArticleId articleId() {
        return articleId;
    }

    public AuthorId authorId() {
        return authorId;
    }

    public CommentId parentCommentId() {
        return parentCommentId;
    }

    public CommentContent content() {
        return content;
    }

    public CommentStatus status() {
        return status;
    }

    public Instant editedAt() {
        return editedAt;
    }

    public Instant deletedAt() {
        return deletedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
