package com.socialmediablog.platform.common.web.correlation;

import java.util.UUID;
import java.util.regex.Pattern;

public final class CorrelationIds {

    public static final int MAX_LENGTH = 128;

    private static final Pattern SAFE_VALUE = Pattern.compile("^[A-Za-z0-9._:-]+$");

    private CorrelationIds() {
    }

    public static String resolve(String candidate) {
        String normalized = normalize(candidate);
        return normalized == null ? generate() : normalized;
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }

    private static String normalize(String candidate) {
        if (candidate == null) {
            return null;
        }
        String value = candidate.trim();
        if (value.isEmpty() || value.length() > MAX_LENGTH || !SAFE_VALUE.matcher(value).matches()) {
            return null;
        }
        return value;
    }
}
