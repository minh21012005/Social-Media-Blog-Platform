package com.socialmediablog.platform.services.follower.domain.aggregate;

import com.socialmediablog.platform.services.follower.domain.model.FollowRelationStatus;
import com.socialmediablog.platform.services.follower.domain.vo.FollowedUserId;
import com.socialmediablog.platform.services.follower.domain.vo.FollowRelationId;
import com.socialmediablog.platform.services.follower.domain.vo.FollowerId;
import java.time.Instant;
import java.util.UUID;

public class FollowRelation {

    private final FollowRelationId id;
    private final FollowerId followerId;
    private final FollowedUserId followedUserId;
    private final FollowRelationStatus status;
    private final Instant followedAt;
    private final Instant unfollowedAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    private FollowRelation(
            FollowRelationId id,
            FollowerId followerId,
            FollowedUserId followedUserId,
            FollowRelationStatus status,
            Instant followedAt,
            Instant unfollowedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        if (followerId != null && followedUserId != null && followerId.value().equals(followedUserId.value())) {
            throw new IllegalArgumentException("User cannot follow themselves");
        }
        this.id = id;
        this.followerId = followerId;
        this.followedUserId = followedUserId;
        this.status = status;
        this.followedAt = followedAt;
        this.unfollowedAt = unfollowedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static FollowRelation follow(FollowerId followerId, FollowedUserId followedUserId, Instant now) {
        return new FollowRelation(
                FollowRelationId.of(UUID.randomUUID()),
                followerId,
                followedUserId,
                FollowRelationStatus.ACTIVE,
                now,
                null,
                now,
                now
        );
    }

    public FollowRelation activate(Instant now) {
        if (status == FollowRelationStatus.ACTIVE) {
            return this;
        }
        if (status == FollowRelationStatus.BLOCKED) {
            throw new IllegalArgumentException("Blocked follow relationship cannot be activated");
        }
        return new FollowRelation(
                id,
                followerId,
                followedUserId,
                FollowRelationStatus.ACTIVE,
                now,
                null,
                createdAt,
                now
        );
    }

    public FollowRelation unfollow(Instant now) {
        if (status == FollowRelationStatus.UNFOLLOWED) {
            return this;
        }
        return new FollowRelation(
                id,
                followerId,
                followedUserId,
                FollowRelationStatus.UNFOLLOWED,
                followedAt,
                now,
                createdAt,
                now
        );
    }

    public boolean isActive() {
        return status == FollowRelationStatus.ACTIVE;
    }

    public static FollowRelation restore(
            UUID id,
            UUID followerId,
            UUID followedUserId,
            FollowRelationStatus status,
            Instant followedAt,
            Instant unfollowedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new FollowRelation(
                FollowRelationId.of(id),
                FollowerId.of(followerId),
                FollowedUserId.of(followedUserId),
                status,
                followedAt,
                unfollowedAt,
                createdAt,
                updatedAt
        );
    }

    public FollowRelationId id() {
        return id;
    }

    public FollowerId followerId() {
        return followerId;
    }

    public FollowedUserId followedUserId() {
        return followedUserId;
    }

    public FollowRelationStatus status() {
        return status;
    }

    public Instant followedAt() {
        return followedAt;
    }

    public Instant unfollowedAt() {
        return unfollowedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
