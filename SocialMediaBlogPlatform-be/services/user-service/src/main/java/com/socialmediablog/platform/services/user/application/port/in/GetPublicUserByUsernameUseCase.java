package com.socialmediablog.platform.services.user.application.port.in;

import com.socialmediablog.platform.services.user.application.result.PublicUserProfile;

public interface GetPublicUserByUsernameUseCase {

    PublicUserProfile executeByUsername(String username);
}
