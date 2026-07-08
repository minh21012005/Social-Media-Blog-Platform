package com.socialmediablog.platform.services.interaction.api.dto;

import com.socialmediablog.platform.services.interaction.application.result.BookmarkView;
import java.time.Instant;
import java.util.UUID;

public record BookmarkResponse(UUID articleId, Instant bookmarkedAt) {

    public static BookmarkResponse from(BookmarkView view) {
        return new BookmarkResponse(view.articleId(), view.bookmarkedAt());
    }
}
