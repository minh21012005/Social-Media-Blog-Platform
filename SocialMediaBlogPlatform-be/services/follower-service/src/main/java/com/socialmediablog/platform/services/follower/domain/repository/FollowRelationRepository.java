package com.socialmediablog.platform.services.follower.domain.repository;

import com.socialmediablog.platform.services.follower.domain.aggregate.FollowRelation;
import com.socialmediablog.platform.services.follower.domain.vo.FollowedUserId;
import com.socialmediablog.platform.services.follower.domain.vo.FollowRelationId;
import com.socialmediablog.platform.services.follower.domain.vo.FollowerId;
import java.util.List;
import java.util.Optional;

public interface FollowRelationRepository {

    Optional<FollowRelation> findById(FollowRelationId id);

    Optional<FollowRelation> findByFollowerIdAndFollowedUserId(FollowerId followerId, FollowedUserId followedUserId);

    List<FollowRelation> findActiveFollowers(FollowedUserId followedUserId, int page, int size);

    List<FollowRelation> findActiveFollowing(FollowerId followerId, int page, int size);

    long countActiveFollowers(FollowedUserId followedUserId);

    long countActiveFollowing(FollowerId followerId);

    FollowRelation save(FollowRelation followRelation);
}
