package com.socialmediablog.platform.services.follower.domain.repository;

import com.socialmediablog.platform.services.follower.domain.aggregate.FollowRelation;
import com.socialmediablog.platform.services.follower.domain.vo.FollowRelationId;
import java.util.Optional;

public interface FollowRelationRepository {

    Optional<FollowRelation> findById(FollowRelationId id);

    FollowRelation save(FollowRelation followRelation);
}
