package com.socialmediablog.platform.services.article.infrastructure.entity;

import com.socialmediablog.platform.common.web.entity.BaseEntity;
import com.socialmediablog.platform.services.article.domain.aggregate.Article;
import com.socialmediablog.platform.services.article.domain.model.ArticleStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "articles")
public class JpaArticleEntity extends BaseEntity {

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(length = 500)
    private String summary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "cover_image_url", length = 2048)
    private String coverImageUrl;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "published_at")
    private Instant publishedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "article_tags", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "tag", nullable = false, length = 50)
    private Set<String> tags = new LinkedHashSet<>();

    protected JpaArticleEntity() {
    }

    private JpaArticleEntity(
            UUID id,
            UUID authorId,
            String title,
            String slug,
            String summary,
            String content,
            String coverImageUrl,
            String status,
            Instant publishedAt,
            Set<String> tags,
            Instant createdAt,
            Instant updatedAt
    ) {
        super(id, createdAt, updatedAt);
        this.authorId = authorId;
        this.title = title;
        this.slug = slug;
        this.summary = summary;
        this.content = content;
        this.coverImageUrl = coverImageUrl;
        this.status = status;
        this.publishedAt = publishedAt;
        this.tags = new LinkedHashSet<>(tags);
    }

    public static JpaArticleEntity fromDomain(Article article) {
        return new JpaArticleEntity(
                article.id().value(),
                article.authorId().value(),
                article.title().value(),
                article.slug().value(),
                article.summary(),
                article.content(),
                article.coverImageUrl(),
                article.status().name(),
                article.publishedAt(),
                article.tags(),
                article.createdAt(),
                article.updatedAt()
        );
    }

    public Article toDomain() {
        return Article.restore(
                id,
                authorId,
                title,
                slug,
                summary,
                content,
                coverImageUrl,
                ArticleStatus.valueOf(status),
                publishedAt,
                tags,
                createdAt,
                updatedAt
        );
    }
}
