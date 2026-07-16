package com.socialmediablog.platform.services.comment.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "comment_claps")
public class JpaCommentClapEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "comment_id", nullable = false)
    private UUID commentId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "clap_count", nullable = false)
    private int clapCount;

    @Column(name = "last_clapped_at", nullable = false)
    private Instant lastClappedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected JpaCommentClapEntity() {
    }

    public JpaCommentClapEntity(
            UUID id,
            UUID commentId,
            UUID userId,
            int clapCount,
            Instant lastClappedAt,
            Instant createdAt
    ) {
        this.id = id;
        this.commentId = commentId;
        this.userId = userId;
        this.clapCount = clapCount;
        this.lastClappedAt = lastClappedAt;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCommentId() {
        return commentId;
    }

    public UUID getUserId() {
        return userId;
    }

    public int getClapCount() {
        return clapCount;
    }

    public Instant getLastClappedAt() {
        return lastClappedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
