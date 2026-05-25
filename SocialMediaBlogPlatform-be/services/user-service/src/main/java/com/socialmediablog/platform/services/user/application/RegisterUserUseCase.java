package com.socialmediablog.platform.services.user.application;

public interface RegisterUserUseCase {

    AuthenticatedUser register(RegisterUserCommand command);
}
