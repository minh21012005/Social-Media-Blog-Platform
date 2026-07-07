package com.socialmediablog.platform.services.follower.api.dto;

import com.socialmediablog.platform.services.follower.application.result.MutualFollowStatus;
import java.util.UUID;

public record MutualFollowResponse(UUID userIdA, UUID userIdB, boolean mutual) {

    public static MutualFollowResponse from(MutualFollowStatus status) {
        return new MutualFollowResponse(status.userIdA(), status.userIdB(), status.mutual());
    }
}
