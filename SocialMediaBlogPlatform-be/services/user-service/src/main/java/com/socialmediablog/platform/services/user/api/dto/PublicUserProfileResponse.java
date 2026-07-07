package com.socialmediablog.platform.services.user.api.dto;

import com.socialmediablog.platform.services.user.application.result.PublicUserProfile;
import java.util.UUID;

public record PublicUserProfileResponse(
        UUID id,
        String username,
        String displayName,
        String bio,
        String avatarUrl,
        boolean isPrivate
) {

    public static PublicUserProfileResponse from(PublicUserProfile profile) {
        return new PublicUserProfileResponse(
                profile.id(),
                profile.username(),
                profile.displayName(),
                profile.bio(),
                profile.avatarUrl(),
                profile.isPrivate()
        );
    }
}
