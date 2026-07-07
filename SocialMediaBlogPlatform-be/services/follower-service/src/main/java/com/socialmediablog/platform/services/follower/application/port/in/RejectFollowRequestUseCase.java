package com.socialmediablog.platform.services.follower.application.port.in;

import com.socialmediablog.platform.services.follower.application.result.FollowRelationView;
import java.util.UUID;

public interface RejectFollowRequestUseCase {
    FollowRelationView reject(UUID userId, UUID followerId);
}
