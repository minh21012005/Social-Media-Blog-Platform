package com.socialmediablog.platform.services.article.infrastructure.entity;

import com.socialmediablog.platform.common.web.entity.BaseEntity;
import com.socialmediablog.platform.services.article.domain.aggregate.ArticleView;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "article_views")
public class JpaArticleViewEntity extends BaseEntity {

    @Column(name = "article_id", nullable = false)
    private UUID articleId;

    @Column(name = "viewer_id")
    private UUID viewerId;

    @Column(name = "anonymous_viewer_key", length = 120)
    private String anonymousViewerKey;

    @Column(length = 80)
    private String source;

    @Column(name = "viewed_at", nullable = false)
    private Instant viewedAt;

    protected JpaArticleViewEntity() {
    }

    private JpaArticleViewEntity(
            UUID id,
            UUID articleId,
            UUID viewerId,
            String anonymousViewerKey,
            String source,
            Instant viewedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        super(id, createdAt, updatedAt);
        this.articleId = articleId;
        this.viewerId = viewerId;
        this.anonymousViewerKey = anonymousViewerKey;
        this.source = source;
        this.viewedAt = viewedAt;
    }

    public static JpaArticleViewEntity fromDomain(ArticleView articleView) {
        return new JpaArticleViewEntity(
                articleView.id().value(),
                articleView.articleId().value(),
                articleView.viewerId(),
                articleView.anonymousViewerKey(),
                articleView.source(),
                articleView.viewedAt(),
                articleView.createdAt(),
                articleView.updatedAt()
        );
    }

    public ArticleView toDomain() {
        return ArticleView.restore(
                id,
                articleId,
                viewerId,
                anonymousViewerKey,
                source,
                viewedAt,
                createdAt,
                updatedAt
        );
    }
}
