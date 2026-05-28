package com.socialmediablog.platform.services.interaction.domain.vo;

import java.util.UUID;

public record BookmarkId(UUID value) {

    public static BookmarkId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Bookmark id is required");
        }
        return new BookmarkId(value);
    }
}
