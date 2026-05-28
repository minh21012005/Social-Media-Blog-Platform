package com.socialmediablog.platform.services.interaction.domain.vo;

import java.util.UUID;

public record ArticleId(UUID value) {

    public static ArticleId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Article id is required");
        }
        return new ArticleId(value);
    }
}
