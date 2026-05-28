package com.socialmediablog.platform.services.article.infrastructure.entity;

import com.socialmediablog.platform.common.web.entity.BaseEntity;
import com.socialmediablog.platform.services.article.domain.aggregate.ArticleStats;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "article_stats")
public class JpaArticleStatsEntity extends BaseEntity {

    @Column(name = "article_id", nullable = false, unique = true)
    private UUID articleId;

    @Column(name = "clap_count", nullable = false)
    private long clapCount;

    @Column(name = "comment_count", nullable = false)
    private long commentCount;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @Column(name = "bookmark_count", nullable = false)
    private long bookmarkCount;

    @Column(name = "last_interaction_at")
    private Instant lastInteractionAt;

    protected JpaArticleStatsEntity() {
    }

    private JpaArticleStatsEntity(
            UUID id,
            UUID articleId,
            long clapCount,
            long commentCount,
            long viewCount,
            long bookmarkCount,
            Instant lastInteractionAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        super(id, createdAt, updatedAt);
        this.articleId = articleId;
        this.clapCount = clapCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
        this.bookmarkCount = bookmarkCount;
        this.lastInteractionAt = lastInteractionAt;
    }

    public static JpaArticleStatsEntity fromDomain(ArticleStats articleStats) {
        return new JpaArticleStatsEntity(
                articleStats.id().value(),
                articleStats.articleId().value(),
                articleStats.clapCount(),
                articleStats.commentCount(),
                articleStats.viewCount(),
                articleStats.bookmarkCount(),
                articleStats.lastInteractionAt(),
                articleStats.createdAt(),
                articleStats.updatedAt()
        );
    }

    public ArticleStats toDomain() {
        return ArticleStats.restore(
                id,
                articleId,
                clapCount,
                commentCount,
                viewCount,
                bookmarkCount,
                lastInteractionAt,
                createdAt,
                updatedAt
        );
    }
}
