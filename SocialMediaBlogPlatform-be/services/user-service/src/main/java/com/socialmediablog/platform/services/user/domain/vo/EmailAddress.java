package com.socialmediablog.platform.services.user.domain.vo;

import java.util.Locale;
import java.util.regex.Pattern;

public record EmailAddress(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public EmailAddress {
        if (value == null) {
            throw new IllegalArgumentException("Email is required");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.length() > 254 || !EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Email is invalid");
        }
    }

    public static EmailAddress of(String value) {
        return new EmailAddress(value);
    }
}
