package com.socialmediablog.platform.services.follower.api.dto;

import com.socialmediablog.platform.services.follower.application.result.FollowCounts;
import java.util.UUID;

public record FollowCountsResponse(UUID userId, long followers, long following) {

    public static FollowCountsResponse from(FollowCounts counts) {
        return new FollowCountsResponse(counts.userId(), counts.followers(), counts.following());
    }
}
