package com.socialmediablog.platform.services.user.application.command;

public record RegisterUserCommand(String username, String email, String password, String displayName) {
}
