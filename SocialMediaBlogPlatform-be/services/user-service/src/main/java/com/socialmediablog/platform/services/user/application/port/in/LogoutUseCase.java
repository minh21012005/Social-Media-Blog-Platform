package com.socialmediablog.platform.services.user.application.port.in;

import com.socialmediablog.platform.services.user.application.command.LogoutCommand;

public interface LogoutUseCase {

    void execute(LogoutCommand command);
}
