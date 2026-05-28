package com.socialmediablog.platform.services.follower.domain.vo;

import java.util.UUID;

public record FollowedUserId(UUID value) {

    public static FollowedUserId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Followed user id is required");
        }
        return new FollowedUserId(value);
    }
}
