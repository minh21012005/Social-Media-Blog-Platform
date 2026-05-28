package com.socialmediablog.platform.services.article.domain.vo;

import java.util.Locale;
import java.util.regex.Pattern;

public record Slug(String value) {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    public static Slug of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Article slug is required");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > 220) {
            throw new IllegalArgumentException("Article slug must not exceed 220 characters");
        }
        if (!SLUG_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Article slug must contain lowercase letters, numbers, and dashes only");
        }
        return new Slug(normalized);
    }
}
