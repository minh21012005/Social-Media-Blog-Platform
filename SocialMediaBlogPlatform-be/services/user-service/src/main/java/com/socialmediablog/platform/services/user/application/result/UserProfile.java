package com.socialmediablog.platform.services.user.application.result;

import com.socialmediablog.platform.services.user.domain.model.Role;
import com.socialmediablog.platform.services.user.domain.aggregate.User;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record UserProfile(
        UUID id,
        String username,
        String email,
        String displayName,
        String bio,
        String avatarUrl,
        Set<String> roles,
        boolean isPrivate,
        Instant createdAt
) {

    public static UserProfile from(User user) {
        return new UserProfile(
                user.id(),
                user.username().value(),
                user.email().value(),
                user.displayName(),
                user.bio(),
                user.avatarUrl(),
                user.roles().stream().map(Role::name).collect(Collectors.toUnmodifiableSet()),
                user.isPrivate(),
                user.createdAt()
        );
    }
}
