package com.socialmediablog.platform.services.user.application;

import com.socialmediablog.platform.services.user.domain.Role;
import com.socialmediablog.platform.services.user.domain.User;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record UserProfile(
        UUID id,
        String username,
        String email,
        String displayName,
        Set<String> roles,
        Instant createdAt
) {

    public static UserProfile from(User user) {
        return new UserProfile(
                user.id(),
                user.username().value(),
                user.email().value(),
                user.displayName(),
                user.roles().stream().map(Role::name).collect(Collectors.toUnmodifiableSet()),
                user.createdAt()
        );
    }
}
