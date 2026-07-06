package com.socialmediablog.platform.services.follower.api.dto;

import com.socialmediablog.platform.services.follower.application.result.FollowStatus;
import java.util.UUID;

public record FollowStatusResponse(UUID viewerId, UUID targetUserId, boolean following, boolean blocked, boolean mutualFollow, boolean pending) {

    public static FollowStatusResponse from(FollowStatus status) {
        return new FollowStatusResponse(status.viewerId(), status.targetUserId(), status.following(), status.blocked(), status.mutualFollow(), status.pending());
    }
}

