package com.socialmediablog.platform.services.comment.infrastructure.entity;

import com.socialmediablog.platform.common.web.entity.BaseEntity;
import com.socialmediablog.platform.services.comment.domain.aggregate.Comment;
import com.socialmediablog.platform.services.comment.domain.model.CommentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "comments")
public class JpaCommentEntity extends BaseEntity {

    @Column(name = "article_id", nullable = false)
    private UUID articleId;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "parent_comment_id")
    private UUID parentCommentId;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "edited_at")
    private Instant editedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected JpaCommentEntity() {
    }

    private JpaCommentEntity(
            UUID id,
            UUID articleId,
            UUID authorId,
            UUID parentCommentId,
            String content,
            String status,
            Instant editedAt,
            Instant deletedAt,
            Instant createdAt,
            Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.articleId = articleId;
        this.authorId = authorId;
        this.parentCommentId = parentCommentId;
        this.content = content;
        this.status = status;
        this.editedAt = editedAt;
        this.deletedAt = deletedAt;
    }

    public static JpaCommentEntity fromDomain(Comment comment) {
        return new JpaCommentEntity(
                comment.id().value(),
                comment.articleId().value(),
                comment.authorId().value(),
                comment.parentCommentId() == null ? null : comment.parentCommentId().value(),
                comment.content().value(),
                comment.status().name(),
                comment.editedAt(),
                comment.deletedAt(),
                comment.createdAt(),
                comment.updatedAt());
    }

    public Comment toDomain() {
        return Comment.restore(
                id,
                articleId,
                authorId,
                parentCommentId,
                content,
                CommentStatus.valueOf(status),
                editedAt,
                deletedAt,
                createdAt,
                updatedAt);
    }
}
