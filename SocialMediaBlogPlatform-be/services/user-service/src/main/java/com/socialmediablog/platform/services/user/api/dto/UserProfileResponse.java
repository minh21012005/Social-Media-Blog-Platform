package com.socialmediablog.platform.services.user.api.dto;

import com.socialmediablog.platform.services.user.application.UserProfile;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String username,
        String email,
        String displayName,
        Set<String> roles,
        Instant createdAt
) {

    public static UserProfileResponse from(UserProfile userProfile) {
        return new UserProfileResponse(
                userProfile.id(),
                userProfile.username(),
                userProfile.email(),
                userProfile.displayName(),
                userProfile.roles(),
                userProfile.createdAt()
        );
    }
}
