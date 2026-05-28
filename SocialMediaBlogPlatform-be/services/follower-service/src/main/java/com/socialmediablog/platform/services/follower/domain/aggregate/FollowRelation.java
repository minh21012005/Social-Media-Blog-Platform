package com.socialmediablog.platform.services.follower.domain.aggregate;

import com.socialmediablog.platform.services.follower.domain.vo.FollowRelationId;
import java.util.UUID;

public class FollowRelation {

    private final FollowRelationId id;

    private FollowRelation(FollowRelationId id) {
        this.id = id;
    }

    public static FollowRelation restore(UUID id) {
        return new FollowRelation(FollowRelationId.of(id));
    }

    public FollowRelationId id() {
        return id;
    }
}
