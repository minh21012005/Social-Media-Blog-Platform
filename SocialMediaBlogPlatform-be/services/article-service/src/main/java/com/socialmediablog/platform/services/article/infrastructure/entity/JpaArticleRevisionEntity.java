package com.socialmediablog.platform.services.article.infrastructure.entity;

import com.socialmediablog.platform.common.web.entity.BaseEntity;
import com.socialmediablog.platform.services.article.domain.aggregate.ArticleRevision;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "article_revisions")
public class JpaArticleRevisionEntity extends BaseEntity {

    @Column(name = "article_id", nullable = false)
    private UUID articleId;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(length = 500)
    private String summary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "cover_image_url", length = 2048)
    private String coverImageUrl;

    @Column(nullable = false)
    private int version;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    protected JpaArticleRevisionEntity() {
    }

    public JpaArticleRevisionEntity(
            UUID id,
            UUID articleId,
            String title,
            String summary,
            String content,
            String coverImageUrl,
            int version,
            UUID createdBy,
            Instant createdAt,
            Instant updatedAt
    ) {
        super(id, createdAt, updatedAt);
        this.articleId = articleId;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.coverImageUrl = coverImageUrl;
        this.version = version;
        this.createdBy = createdBy;
    }

    public static JpaArticleRevisionEntity fromDomain(ArticleRevision revision) {
        return new JpaArticleRevisionEntity(
                revision.id(),
                revision.articleId().value(),
                revision.title(),
                revision.summary(),
                revision.content(),
                revision.coverImageUrl(),
                revision.version(),
                revision.createdBy().value(),
                revision.createdAt(),
                revision.updatedAt()
        );
    }

    public ArticleRevision toDomain() {
        return ArticleRevision.restore(
                id,
                articleId,
                title,
                summary,
                content,
                coverImageUrl,
                version,
                createdBy,
                createdAt,
                updatedAt
        );
    }
}
