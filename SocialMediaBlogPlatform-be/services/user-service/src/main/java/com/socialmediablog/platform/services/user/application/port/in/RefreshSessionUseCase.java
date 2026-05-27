package com.socialmediablog.platform.services.user.application.port.in;

import com.socialmediablog.platform.services.user.application.command.RefreshSessionCommand;
import com.socialmediablog.platform.services.user.application.result.AuthenticatedUser;

public interface RefreshSessionUseCase {

    AuthenticatedUser execute(RefreshSessionCommand command);
}
