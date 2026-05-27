package com.socialmediablog.platform.services.user.application.result;

public record IssuedRefreshToken(String refreshToken, long expiresInSeconds) {
}
