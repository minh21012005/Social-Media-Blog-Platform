package com.socialmediablog.platform.services.user.application;

public record RegisterUserCommand(String username, String email, String password, String displayName) {
}
