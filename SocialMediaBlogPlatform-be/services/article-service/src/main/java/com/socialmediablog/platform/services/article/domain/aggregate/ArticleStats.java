package com.socialmediablog.platform.services.article.domain.aggregate;

import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import com.socialmediablog.platform.services.article.domain.vo.ArticleStatsId;
import java.time.Instant;
import java.util.UUID;

public class ArticleStats {

    private final ArticleStatsId id;
    private final ArticleId articleId;
    private final long clapCount;
    private final long commentCount;
    private final long viewCount;
    private final long bookmarkCount;
    private final Instant lastInteractionAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    private ArticleStats(
            ArticleStatsId id,
            ArticleId articleId,
            long clapCount,
            long commentCount,
            long viewCount,
            long bookmarkCount,
            Instant lastInteractionAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.articleId = articleId;
        this.clapCount = nonNegative(clapCount, "clap count");
        this.commentCount = nonNegative(commentCount, "comment count");
        this.viewCount = nonNegative(viewCount, "view count");
        this.bookmarkCount = nonNegative(bookmarkCount, "bookmark count");
        this.lastInteractionAt = lastInteractionAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ArticleStats empty(ArticleId articleId, Instant now) {
        return new ArticleStats(ArticleStatsId.of(UUID.randomUUID()), articleId, 0, 0, 0, 0, null, now, now);
    }

    public static ArticleStats restore(
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
        return new ArticleStats(
                ArticleStatsId.of(id),
                ArticleId.of(articleId),
                clapCount,
                commentCount,
                viewCount,
                bookmarkCount,
                lastInteractionAt,
                createdAt,
                updatedAt
        );
    }

    public ArticleStats recordView(Instant now) {
        return new ArticleStats(
                id,
                articleId,
                clapCount,
                commentCount,
                viewCount + 1,
                bookmarkCount,
                now,
                createdAt,
                now
        );
    }

    private static long nonNegative(long value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException("Article " + field + " must not be negative");
        }
        return value;
    }

    public ArticleStatsId id() {
        return id;
    }

    public ArticleId articleId() {
        return articleId;
    }

    public long clapCount() {
        return clapCount;
    }

    public long commentCount() {
        return commentCount;
    }

    public long viewCount() {
        return viewCount;
    }

    public long bookmarkCount() {
        return bookmarkCount;
    }

    public Instant lastInteractionAt() {
        return lastInteractionAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
