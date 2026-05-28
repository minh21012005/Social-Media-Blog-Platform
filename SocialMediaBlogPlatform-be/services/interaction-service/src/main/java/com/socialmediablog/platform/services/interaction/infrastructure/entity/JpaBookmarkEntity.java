package com.socialmediablog.platform.services.interaction.infrastructure.entity;

import com.socialmediablog.platform.common.web.entity.BaseEntity;
import com.socialmediablog.platform.services.interaction.domain.aggregate.Bookmark;
import com.socialmediablog.platform.services.interaction.domain.model.BookmarkStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "bookmarks",
        uniqueConstraints = @UniqueConstraint(name = "uk_bookmarks_user_article", columnNames = {"user_id", "article_id"})
)
public class JpaBookmarkEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "article_id", nullable = false)
    private UUID articleId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "bookmarked_at", nullable = false)
    private Instant bookmarkedAt;

    @Column(name = "removed_at")
    private Instant removedAt;

    protected JpaBookmarkEntity() {
    }

    private JpaBookmarkEntity(
            UUID id,
            UUID userId,
            UUID articleId,
            String status,
            Instant bookmarkedAt,
            Instant removedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        super(id, createdAt, updatedAt);
        this.userId = userId;
        this.articleId = articleId;
        this.status = status;
        this.bookmarkedAt = bookmarkedAt;
        this.removedAt = removedAt;
    }

    public static JpaBookmarkEntity fromDomain(Bookmark bookmark) {
        return new JpaBookmarkEntity(
                bookmark.id().value(),
                bookmark.userId().value(),
                bookmark.articleId().value(),
                bookmark.status().name(),
                bookmark.bookmarkedAt(),
                bookmark.removedAt(),
                bookmark.createdAt(),
                bookmark.updatedAt()
        );
    }

    public Bookmark toDomain() {
        return Bookmark.restore(
                id,
                userId,
                articleId,
                BookmarkStatus.valueOf(status),
                bookmarkedAt,
                removedAt,
                createdAt,
                updatedAt
        );
    }
}
