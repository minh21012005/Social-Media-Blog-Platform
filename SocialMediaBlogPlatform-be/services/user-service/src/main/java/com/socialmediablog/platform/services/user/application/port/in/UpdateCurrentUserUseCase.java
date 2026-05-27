package com.socialmediablog.platform.services.user.application.port.in;

import com.socialmediablog.platform.services.user.application.command.UpdateUserProfileCommand;
import com.socialmediablog.platform.services.user.application.result.UserProfile;

public interface UpdateCurrentUserUseCase {

    UserProfile execute(UpdateUserProfileCommand command);
}
