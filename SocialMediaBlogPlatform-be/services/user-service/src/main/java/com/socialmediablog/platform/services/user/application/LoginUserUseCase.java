package com.socialmediablog.platform.services.user.application;

public interface LoginUserUseCase {

    AuthenticatedUser login(LoginUserCommand command);
}
