package com.socialmediablog.platform.services.interaction.domain.aggregate;

import com.socialmediablog.platform.services.interaction.domain.model.BookmarkStatus;
import com.socialmediablog.platform.services.interaction.domain.vo.ArticleId;
import com.socialmediablog.platform.services.interaction.domain.vo.BookmarkId;
import com.socialmediablog.platform.services.interaction.domain.vo.InteractorId;
import java.time.Instant;
import java.util.UUID;

public class Bookmark {

    private final BookmarkId id;
    private final InteractorId userId;
    private final ArticleId articleId;
    private final BookmarkStatus status;
    private final Instant bookmarkedAt;
    private final Instant removedAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Bookmark(
            BookmarkId id,
            InteractorId userId,
            ArticleId articleId,
            BookmarkStatus status,
            Instant bookmarkedAt,
            Instant removedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.articleId = articleId;
        this.status = status;
        this.bookmarkedAt = bookmarkedAt;
        this.removedAt = removedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Bookmark create(InteractorId userId, ArticleId articleId, Instant now) {
        return new Bookmark(BookmarkId.of(UUID.randomUUID()), userId, articleId, BookmarkStatus.ACTIVE, now, null, now, now);
    }

    public static Bookmark restore(
            UUID id,
            UUID userId,
            UUID articleId,
            BookmarkStatus status,
            Instant bookmarkedAt,
            Instant removedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Bookmark(
                BookmarkId.of(id),
                InteractorId.of(userId),
                ArticleId.of(articleId),
                status,
                bookmarkedAt,
                removedAt,
                createdAt,
                updatedAt
        );
    }

    public BookmarkId id() {
        return id;
    }

    public InteractorId userId() {
        return userId;
    }

    public ArticleId articleId() {
        return articleId;
    }

    public BookmarkStatus status() {
        return status;
    }

    public Instant bookmarkedAt() {
        return bookmarkedAt;
    }

    public Instant removedAt() {
        return removedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
