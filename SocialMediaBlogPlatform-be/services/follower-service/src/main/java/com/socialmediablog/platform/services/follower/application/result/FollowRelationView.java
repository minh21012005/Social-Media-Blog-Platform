package com.socialmediablog.platform.services.follower.application.result;

import com.socialmediablog.platform.services.follower.domain.aggregate.FollowRelation;
import java.time.Instant;
import java.util.UUID;

public record FollowRelationView(
        UUID id,
        UUID followerId,
        UUID followedUserId,
        boolean following,
        boolean blocked,
        Instant followedAt,
        Instant unfollowedAt
) {

    public static FollowRelationView from(FollowRelation relation) {
        return new FollowRelationView(
                relation.id().value(),
                relation.followerId().value(),
                relation.followedUserId().value(),
                relation.isActive(),
                relation.isBlocked(),
                relation.followedAt(),
                relation.unfollowedAt()
        );
    }
}

