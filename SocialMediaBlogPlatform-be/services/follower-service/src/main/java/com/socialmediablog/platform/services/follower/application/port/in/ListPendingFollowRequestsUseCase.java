package com.socialmediablog.platform.services.follower.application.port.in;

import com.socialmediablog.platform.services.follower.application.result.FollowUserPage;
import java.util.UUID;

public interface ListPendingFollowRequestsUseCase {
    FollowUserPage listPending(UUID userId, int page, int size);
}
