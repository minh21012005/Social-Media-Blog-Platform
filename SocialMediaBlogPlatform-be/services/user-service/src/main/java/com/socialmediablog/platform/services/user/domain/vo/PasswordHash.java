package com.socialmediablog.platform.services.user.domain.vo;

public record PasswordHash(String value) {

    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Password hash is required");
        }
    }

    public static PasswordHash of(String value) {
        return new PasswordHash(value);
    }
}
