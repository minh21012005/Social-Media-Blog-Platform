package com.socialmediablog.platform.services.follower.api.dto;

import com.socialmediablog.platform.services.follower.application.result.FollowUserPage;
import java.util.List;
import java.util.UUID;

public record FollowUserPageResponse(
        UUID userId,
        List<FollowUserItemResponse> users,
        int page,
        int size,
        long total
) {

    public static FollowUserPageResponse from(FollowUserPage page) {
        return new FollowUserPageResponse(
                page.userId(),
                page.users().stream().map(FollowUserItemResponse::from).toList(),
                page.page(),
                page.size(),
                page.total()
        );
    }
}
