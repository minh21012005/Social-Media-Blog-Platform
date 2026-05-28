package com.socialmediablog.platform.services.follower.domain.repository;

import com.socialmediablog.platform.services.follower.domain.aggregate.FollowRelation;
import com.socialmediablog.platform.services.follower.domain.vo.FollowedUserId;
import com.socialmediablog.platform.services.follower.domain.vo.FollowRelationId;
import com.socialmediablog.platform.services.follower.domain.vo.FollowerId;
import java.util.Optional;

public interface FollowRelationRepository {

    Optional<FollowRelation> findById(FollowRelationId id);

    Optional<FollowRelation> findByFollowerIdAndFollowedUserId(FollowerId followerId, FollowedUserId followedUserId);

    FollowRelation save(FollowRelation followRelation);
}
