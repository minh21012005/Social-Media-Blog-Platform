package com.socialmediablog.platform.services.follower.infrastructure.persistence;

import com.socialmediablog.platform.services.follower.infrastructure.entity.JpaFollowRelationEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaFollowRelationRepository extends JpaRepository<JpaFollowRelationEntity, UUID> {

    Optional<JpaFollowRelationEntity> findByFollowerIdAndFollowedUserId(UUID followerId, UUID followedUserId);
}
