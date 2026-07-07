package com.socialmediablog.platform.services.follower.application.port.in;

import java.util.UUID;

public interface GetMuteStatusUseCase {
    boolean isMuted(UUID muterId, UUID mutedUserId);
}
