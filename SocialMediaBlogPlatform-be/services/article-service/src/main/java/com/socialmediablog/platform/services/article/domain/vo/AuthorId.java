package com.socialmediablog.platform.services.article.domain.vo;

import java.util.UUID;

public record AuthorId(UUID value) {

    public static AuthorId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Author id is required");
        }
        return new AuthorId(value);
    }
}
