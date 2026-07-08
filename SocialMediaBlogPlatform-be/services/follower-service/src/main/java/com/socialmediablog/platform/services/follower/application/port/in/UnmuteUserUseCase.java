package com.socialmediablog.platform.services.follower.application.port.in;

import java.util.UUID;

public interface UnmuteUserUseCase {
    void unmute(UUID muterId, UUID mutedUserId);
}
