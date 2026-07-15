package com.socialmediablog.platform.services.user.application.port.in;

import com.socialmediablog.platform.services.user.application.command.GoogleLoginCommand;
import com.socialmediablog.platform.services.user.application.result.AuthenticatedUser;

public interface LoginWithGoogleUseCase {

    AuthenticatedUser execute(GoogleLoginCommand command);
}