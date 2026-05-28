package com.socialmediablog.platform.services.comment.infrastructure.entity;

import com.socialmediablog.platform.common.web.entity.BaseEntity;
import com.socialmediablog.platform.services.comment.domain.aggregate.CommentStats;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "comment_stats")
public class JpaCommentStatsEntity extends BaseEntity {

    @Column(name = "comment_id", nullable = false, unique = true)
    private UUID commentId;

    @Column(name = "clap_count", nullable = false)
    private long clapCount;

    @Column(name = "reply_count", nullable = false)
    private long replyCount;

    @Column(name = "last_interaction_at")
    private Instant lastInteractionAt;

    protected JpaCommentStatsEntity() {
    }

    private JpaCommentStatsEntity(
            UUID id,
            UUID commentId,
            long clapCount,
            long replyCount,
            Instant lastInteractionAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        super(id, createdAt, updatedAt);
        this.commentId = commentId;
        this.clapCount = clapCount;
        this.replyCount = replyCount;
        this.lastInteractionAt = lastInteractionAt;
    }

    public static JpaCommentStatsEntity fromDomain(CommentStats commentStats) {
        return new JpaCommentStatsEntity(
                commentStats.id().value(),
                commentStats.commentId().value(),
                commentStats.clapCount(),
                commentStats.replyCount(),
                commentStats.lastInteractionAt(),
                commentStats.createdAt(),
                commentStats.updatedAt()
        );
    }

    public CommentStats toDomain() {
        return CommentStats.restore(
                id,
                commentId,
                clapCount,
                replyCount,
                lastInteractionAt,
                createdAt,
                updatedAt
        );
    }
}
