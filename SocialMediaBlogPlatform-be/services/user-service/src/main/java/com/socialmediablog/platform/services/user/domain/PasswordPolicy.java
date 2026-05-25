package com.socialmediablog.platform.services.user.domain;

public final class PasswordPolicy {

    private PasswordPolicy() {
    }

    public static void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 8 || rawPassword.length() > 72) {
            throw new IllegalArgumentException("Password must be between 8 and 72 characters");
        }
    }
}
