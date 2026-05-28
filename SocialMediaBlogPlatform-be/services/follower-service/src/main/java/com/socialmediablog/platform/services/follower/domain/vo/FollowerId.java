package com.socialmediablog.platform.services.follower.domain.vo;

import java.util.UUID;

public record FollowerId(UUID value) {

    public static FollowerId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Follower id is required");
        }
        return new FollowerId(value);
    }
}
