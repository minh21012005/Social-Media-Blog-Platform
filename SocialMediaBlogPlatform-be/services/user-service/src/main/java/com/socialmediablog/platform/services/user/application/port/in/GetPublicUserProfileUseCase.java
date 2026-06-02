package com.socialmediablog.platform.services.user.application.port.in;

import com.socialmediablog.platform.services.user.application.result.PublicUserProfile;
import java.util.UUID;

public interface GetPublicUserProfileUseCase {

    PublicUserProfile executePublic(UUID userId);
}
