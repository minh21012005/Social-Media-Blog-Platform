package com.socialmediablog.platform.services.user.application.result;

import com.socialmediablog.platform.services.user.domain.aggregate.User;
import java.util.UUID;

public record PublicUserProfile(
        UUID id,
        String username,
        String displayName,
        String bio,
        String avatarUrl
) {

    public static PublicUserProfile from(User user) {
        return new PublicUserProfile(
                user.id(),
                user.username().value(),
                user.displayName(),
                user.bio(),
                user.avatarUrl()
        );
    }
}
