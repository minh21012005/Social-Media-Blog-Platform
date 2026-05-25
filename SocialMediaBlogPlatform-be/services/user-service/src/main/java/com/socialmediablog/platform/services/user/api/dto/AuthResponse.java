package com.socialmediablog.platform.services.user.api.dto;

import com.socialmediablog.platform.services.user.application.AuthenticatedUser;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UserProfileResponse user
) {

    public static AuthResponse from(AuthenticatedUser authenticatedUser) {
        return new AuthResponse(
                authenticatedUser.token().accessToken(),
                authenticatedUser.token().tokenType(),
                authenticatedUser.token().expiresInSeconds(),
                UserProfileResponse.from(authenticatedUser.user())
        );
    }
}
