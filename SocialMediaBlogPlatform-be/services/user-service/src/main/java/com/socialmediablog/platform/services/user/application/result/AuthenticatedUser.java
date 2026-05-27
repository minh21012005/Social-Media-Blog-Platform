package com.socialmediablog.platform.services.user.application.result;

public record AuthenticatedUser(UserProfile user, IssuedToken token, IssuedRefreshToken refreshToken) {
}
