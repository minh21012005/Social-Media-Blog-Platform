package com.socialmediablog.platform.services.user.api.dto;

import com.socialmediablog.platform.services.user.application.result.UserProfile;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserProfileResponse(
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

    public static UserProfileResponse from(UserProfile userProfile) {
        return new UserProfileResponse(
                userProfile.id(),
                userProfile.username(),
                userProfile.email(),
                userProfile.displayName(),
                userProfile.bio(),
                userProfile.avatarUrl(),
                userProfile.roles(),
                userProfile.isPrivate(),
                userProfile.createdAt()
        );
    }
}
