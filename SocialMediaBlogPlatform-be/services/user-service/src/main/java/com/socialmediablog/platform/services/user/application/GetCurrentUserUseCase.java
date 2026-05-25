package com.socialmediablog.platform.services.user.application;

import java.util.UUID;

public interface GetCurrentUserUseCase {

    UserProfile getCurrentUser(UUID userId);
}
