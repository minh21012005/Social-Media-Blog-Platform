package com.socialmediablog.platform.services.user.application.port.in;

import com.socialmediablog.platform.services.user.application.command.ChangePasswordCommand;

public interface ChangePasswordUseCase {

    void execute(ChangePasswordCommand command);
}
