package com.socialmediablog.platform.services.follower.api.dto;

import com.socialmediablog.platform.services.follower.application.result.BlockStatus;
import java.util.UUID;

public record BlockStatusResponse(UUID viewerId, UUID targetUserId, boolean blocked) {

    public static BlockStatusResponse from(BlockStatus status) {
        return new BlockStatusResponse(status.viewerId(), status.targetUserId(), status.blocked());
    }
}
