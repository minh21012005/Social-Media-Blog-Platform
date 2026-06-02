package com.socialmediablog.platform.services.follower.application.result;

import java.util.List;
import java.util.UUID;

public record FollowUserPage(UUID userId, List<FollowUserItem> users, int page, int size, long total) {
}
