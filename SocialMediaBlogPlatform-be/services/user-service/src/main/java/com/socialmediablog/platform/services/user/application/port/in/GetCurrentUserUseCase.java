package com.socialmediablog.platform.services.user.application.port.in;

import com.socialmediablog.platform.services.user.application.result.UserProfile;
import java.util.UUID;

public interface GetCurrentUserUseCase {

    UserProfile execute(UUID userId);
}
