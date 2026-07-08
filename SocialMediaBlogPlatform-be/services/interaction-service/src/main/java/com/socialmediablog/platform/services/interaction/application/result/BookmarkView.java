package com.socialmediablog.platform.services.interaction.application.result;

import com.socialmediablog.platform.services.interaction.domain.aggregate.Bookmark;
import java.time.Instant;
import java.util.UUID;

public record BookmarkView(UUID articleId, Instant bookmarkedAt) {

    public static BookmarkView from(Bookmark bookmark) {
        return new BookmarkView(bookmark.articleId().value(), bookmark.bookmarkedAt());
    }
}
