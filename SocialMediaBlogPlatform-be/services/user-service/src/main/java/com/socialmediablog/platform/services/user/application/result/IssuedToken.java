package com.socialmediablog.platform.services.user.application.result;

public record IssuedToken(String accessToken, String tokenType, long expiresInSeconds) {
}
