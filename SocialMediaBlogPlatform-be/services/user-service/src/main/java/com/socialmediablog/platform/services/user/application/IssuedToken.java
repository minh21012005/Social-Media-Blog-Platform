package com.socialmediablog.platform.services.user.application;

public record IssuedToken(String accessToken, String tokenType, long expiresInSeconds) {
}
