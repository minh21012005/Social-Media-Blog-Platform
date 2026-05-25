package com.socialmediablog.platform.common.security;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.jwt.Jwt;

public final class JwtSupport {

    private JwtSupport() {
    }

    public static SecretKey hmacKey(String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes for HS256");
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public static Set<String> rolesFrom(Jwt jwt) {
        Object roles = jwt.getClaims().get("roles");
        if (roles instanceof Collection<?> collection) {
            return collection.stream()
                    .map(String::valueOf)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        if (roles instanceof String value && !value.isBlank()) {
            return Set.of(value.split(","));
        }
        return Set.of();
    }

    public static String rolesHeader(Set<String> roles) {
        return roles == null ? "" : String.join(",", roles);
    }
}
