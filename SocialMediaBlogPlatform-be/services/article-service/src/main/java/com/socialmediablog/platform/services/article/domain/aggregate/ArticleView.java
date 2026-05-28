package com.socialmediablog.platform.services.article.domain.aggregate;

import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import com.socialmediablog.platform.services.article.domain.vo.ArticleViewId;
import java.time.Instant;
import java.util.UUID;

public class ArticleView {

    private final ArticleViewId id;
    private final ArticleId articleId;
    private final UUID viewerId;
    private final String anonymousViewerKey;
    private final String source;
    private final Instant viewedAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    private ArticleView(
            ArticleViewId id,
            ArticleId articleId,
            UUID viewerId,
            String anonymousViewerKey,
            String source,
            Instant viewedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.articleId = articleId;
        this.viewerId = viewerId;
        this.anonymousViewerKey = normalize(anonymousViewerKey, 120, "anonymous viewer key");
        this.source = normalize(source, 80, "view source");
        this.viewedAt = viewedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ArticleView record(ArticleId articleId, UUID viewerId, String anonymousViewerKey, String source, Instant now) {
        return new ArticleView(ArticleViewId.of(UUID.randomUUID()), articleId, viewerId, anonymousViewerKey, source, now, now, now);
    }

    public static ArticleView restore(
            UUID id,
            UUID articleId,
            UUID viewerId,
            String anonymousViewerKey,
            String source,
            Instant viewedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new ArticleView(
                ArticleViewId.of(id),
                ArticleId.of(articleId),
                viewerId,
                anonymousViewerKey,
                source,
                viewedAt,
                createdAt,
                updatedAt
        );
    }

    private static String normalize(String value, int maxLength, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException("Article " + field + " must not exceed " + maxLength + " characters");
        }
        return normalized;
    }

    public ArticleViewId id() {
        return id;
    }

    public ArticleId articleId() {
        return articleId;
    }

    public UUID viewerId() {
        return viewerId;
    }

    public String anonymousViewerKey() {
        return anonymousViewerKey;
    }

    public String source() {
        return source;
    }

    public Instant viewedAt() {
        return viewedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
