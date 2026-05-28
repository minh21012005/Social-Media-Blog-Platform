package com.socialmediablog.platform.services.comment.domain.vo;

public record CommentContent(String value) {

    public static CommentContent of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Comment content is required");
        }
        String normalized = value.trim();
        if (normalized.length() > 5000) {
            throw new IllegalArgumentException("Comment content must not exceed 5000 characters");
        }
        return new CommentContent(normalized);
    }
}
