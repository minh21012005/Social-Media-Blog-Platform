package com.socialmediablog.platform.services.notification.infrastructure.feign;

import java.util.UUID;

public record UserPublicProfileResponse(
        UUID id,
        String username,
        String displayName,
        String bio,
        String avatarUrl
) {
}
