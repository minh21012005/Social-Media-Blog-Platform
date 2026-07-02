package com.socialmediablog.platform.services.follower.api.dto;

import com.socialmediablog.platform.services.follower.application.result.FollowRelationView;
import java.time.Instant;
import java.util.UUID;

public record FollowRelationResponse(
        UUID id,
        UUID followerId,
        UUID followedUserId,
        boolean following,
        boolean blocked,
        Instant followedAt,
        Instant unfollowedAt
) {

    public static FollowRelationResponse from(FollowRelationView view) {
        return new FollowRelationResponse(
                view.id(),
                view.followerId(),
                view.followedUserId(),
                view.following(),
                view.blocked(),
                view.followedAt(),
                view.unfollowedAt()
        );
    }
}

