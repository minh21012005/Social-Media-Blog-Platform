package com.socialmediablog.platform.services.user.domain.vo;

import java.util.Locale;
import java.util.regex.Pattern;

public record Username(String value) {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9._]{3,30}$");

    public Username {
        if (value == null) {
            throw new IllegalArgumentException("Username is required");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (!USERNAME_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Username must be 3-30 characters and use letters, numbers, dots, or underscores");
        }
    }

    public static Username of(String value) {
        return new Username(value);
    }
}
