package com.socialmediablog.platform.services.follower.api.dto;

import com.socialmediablog.platform.services.follower.application.result.FollowUserItem;
import java.time.Instant;
import java.util.UUID;

public record FollowUserItemResponse(UUID userId, Instant followedAt) {

    public static FollowUserItemResponse from(FollowUserItem item) {
        return new FollowUserItemResponse(item.userId(), item.followedAt());
    }
}
