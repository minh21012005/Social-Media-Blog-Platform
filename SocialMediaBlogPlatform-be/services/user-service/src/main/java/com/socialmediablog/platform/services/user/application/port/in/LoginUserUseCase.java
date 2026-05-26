package com.socialmediablog.platform.services.user.application.port.in;

import com.socialmediablog.platform.services.user.application.command.LoginUserCommand;
import com.socialmediablog.platform.services.user.application.result.AuthenticatedUser;

public interface LoginUserUseCase {

    AuthenticatedUser execute(LoginUserCommand command);
}
