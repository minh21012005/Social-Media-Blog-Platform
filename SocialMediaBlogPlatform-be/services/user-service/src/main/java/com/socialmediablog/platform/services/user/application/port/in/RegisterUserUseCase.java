package com.socialmediablog.platform.services.user.application.port.in;

import com.socialmediablog.platform.services.user.application.command.RegisterUserCommand;
import com.socialmediablog.platform.services.user.application.result.AuthenticatedUser;

public interface RegisterUserUseCase {

    AuthenticatedUser execute(RegisterUserCommand command);
}
