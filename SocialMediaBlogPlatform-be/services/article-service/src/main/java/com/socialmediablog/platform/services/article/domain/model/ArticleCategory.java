package com.socialmediablog.platform.services.article.domain.model;

import java.util.Arrays;
import java.util.Locale;

public enum ArticleCategory {
    DESIGN("design"),
    CULTURE("culture"),
    TECHNOLOGY("technology"),
    LIFESTYLE("lifestyle");

    private final String slug;

    ArticleCategory(String slug) {
        this.slug = slug;
    }

    public String slug() {
        return slug;
    }

    public static ArticleCategory fromSlug(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Article category is required");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(category -> category.slug.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported article category: " + value));
    }
}
