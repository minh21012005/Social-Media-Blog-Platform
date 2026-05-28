package com.socialmediablog.platform.services.article.domain.aggregate;

import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import com.socialmediablog.platform.services.article.domain.vo.AuthorId;
import java.time.Instant;
import java.util.UUID;

public class ArticleRevision {

    private final UUID id;
    private final ArticleId articleId;
    private final String title;
    private final String summary;
    private final String content;
    private final String coverImageUrl;
    private final int version;
    private final AuthorId createdBy;
    private final Instant createdAt;
    private final Instant updatedAt;

    private ArticleRevision(
            UUID id,
            ArticleId articleId,
            String title,
            String summary,
            String content,
            String coverImageUrl,
            int version,
            AuthorId createdBy,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.articleId = articleId;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.coverImageUrl = coverImageUrl;
        this.version = version;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ArticleRevision restore(
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
        return new ArticleRevision(
                id,
                ArticleId.of(articleId),
                title,
                summary,
                content,
                coverImageUrl,
                version,
                AuthorId.of(createdBy),
                createdAt,
                updatedAt
        );
    }

    public UUID id() {
        return id;
    }

    public ArticleId articleId() {
        return articleId;
    }

    public String title() {
        return title;
    }

    public String summary() {
        return summary;
    }

    public String content() {
        return content;
    }

    public String coverImageUrl() {
        return coverImageUrl;
    }

    public int version() {
        return version;
    }

    public AuthorId createdBy() {
        return createdBy;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
