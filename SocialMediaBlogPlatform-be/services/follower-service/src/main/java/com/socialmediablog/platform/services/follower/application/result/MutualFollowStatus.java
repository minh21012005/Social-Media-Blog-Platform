package com.socialmediablog.platform.services.follower.application.result;

import java.util.UUID;

public record MutualFollowStatus(UUID userIdA, UUID userIdB, boolean mutual) {
}
