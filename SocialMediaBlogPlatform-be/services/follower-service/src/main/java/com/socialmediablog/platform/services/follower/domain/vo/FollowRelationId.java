package com.socialmediablog.platform.services.follower.domain.vo;

import java.util.UUID;

public record FollowRelationId(UUID value) {

    public static FollowRelationId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Follow relation id is required");
        }
        return new FollowRelationId(value);
    }
}
