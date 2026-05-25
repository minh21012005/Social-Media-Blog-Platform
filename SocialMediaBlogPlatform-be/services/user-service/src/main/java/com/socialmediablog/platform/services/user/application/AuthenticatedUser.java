package com.socialmediablog.platform.services.user.application;

public record AuthenticatedUser(UserProfile user, IssuedToken token) {
}
