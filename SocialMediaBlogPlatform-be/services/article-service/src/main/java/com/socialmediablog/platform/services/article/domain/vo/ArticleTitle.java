package com.socialmediablog.platform.services.article.domain.vo;

public record ArticleTitle(String value) {

    public static ArticleTitle of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Article title is required");
        }
        String normalized = value.trim();
        if (normalized.length() > 180) {
            throw new IllegalArgumentException("Article title must not exceed 180 characters");
        }
        return new ArticleTitle(normalized);
    }
}
