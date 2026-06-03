package com.socialmediablog.platform.services.follower.infrastructure.persistence;

import com.socialmediablog.platform.services.follower.infrastructure.entity.JpaFollowRelationEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaFollowRelationRepository extends JpaRepository<JpaFollowRelationEntity, UUID> {

    Optional<JpaFollowRelationEntity> findByFollowerIdAndFollowedUserId(UUID followerId, UUID followedUserId);

    List<JpaFollowRelationEntity> findByFollowedUserIdAndStatusOrderByFollowedAtDesc(
            UUID followedUserId,
            String status,
            Pageable pageable
    );

    List<JpaFollowRelationEntity> findByFollowerIdAndStatusOrderByFollowedAtDesc(
            UUID followerId,
            String status,
            Pageable pageable
    );

    long countByFollowedUserIdAndStatus(UUID followedUserId, String status);

    long countByFollowerIdAndStatus(UUID followerId, String status);
}
