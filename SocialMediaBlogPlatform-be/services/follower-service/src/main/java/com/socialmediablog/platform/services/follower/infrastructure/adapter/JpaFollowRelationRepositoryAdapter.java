package com.socialmediablog.platform.services.follower.infrastructure.adapter;

import com.socialmediablog.platform.services.follower.domain.aggregate.FollowRelation;
import com.socialmediablog.platform.services.follower.domain.model.FollowRelationStatus;
import com.socialmediablog.platform.services.follower.domain.repository.FollowRelationRepository;
import com.socialmediablog.platform.services.follower.domain.vo.FollowedUserId;
import com.socialmediablog.platform.services.follower.domain.vo.FollowRelationId;
import com.socialmediablog.platform.services.follower.domain.vo.FollowerId;
import com.socialmediablog.platform.services.follower.infrastructure.entity.JpaFollowRelationEntity;
import com.socialmediablog.platform.services.follower.infrastructure.persistence.SpringDataJpaFollowRelationRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class JpaFollowRelationRepositoryAdapter implements FollowRelationRepository {

    private final SpringDataJpaFollowRelationRepository repository;

    public JpaFollowRelationRepositoryAdapter(SpringDataJpaFollowRelationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<FollowRelation> findById(FollowRelationId id) {
        return repository.findById(id.value()).map(JpaFollowRelationEntity::toDomain);
    }

    @Override
    public Optional<FollowRelation> findByFollowerIdAndFollowedUserId(
            FollowerId followerId,
            FollowedUserId followedUserId
    ) {
        return repository.findByFollowerIdAndFollowedUserId(followerId.value(), followedUserId.value())
                .map(JpaFollowRelationEntity::toDomain);
    }

    @Override
    public List<FollowRelation> findActiveFollowers(FollowedUserId followedUserId, int page, int size) {
        return repository.findByFollowedUserIdAndStatusOrderByFollowedAtDesc(
                followedUserId.value(),
                FollowRelationStatus.ACTIVE.name(),
                PageRequest.of(page, size)
        ).stream().map(JpaFollowRelationEntity::toDomain).toList();
    }

    @Override
    public List<FollowRelation> findActiveFollowing(FollowerId followerId, int page, int size) {
        return repository.findByFollowerIdAndStatusOrderByFollowedAtDesc(
                followerId.value(),
                FollowRelationStatus.ACTIVE.name(),
                PageRequest.of(page, size)
        ).stream().map(JpaFollowRelationEntity::toDomain).toList();
    }

    @Override
    public long countActiveFollowers(FollowedUserId followedUserId) {
        return repository.countByFollowedUserIdAndStatus(followedUserId.value(), FollowRelationStatus.ACTIVE.name());
    }

    @Override
    public long countActiveFollowing(FollowerId followerId) {
        return repository.countByFollowerIdAndStatus(followerId.value(), FollowRelationStatus.ACTIVE.name());
    }

    @Override
    public List<FollowRelation> findBlockedByUser(FollowerId blockerId, int page, int size) {
        return repository.findByFollowerIdAndStatusOrderByUpdatedAtDesc(
                blockerId.value(),
                FollowRelationStatus.BLOCKED.name(),
                PageRequest.of(page, size)
        ).stream().map(JpaFollowRelationEntity::toDomain).toList();
    }

    @Override
    public long countBlockedByUser(FollowerId blockerId) {
        return repository.countByFollowerIdAndStatus(blockerId.value(), FollowRelationStatus.BLOCKED.name());
    }

    @Override
    public FollowRelation save(FollowRelation followRelation) {
        return repository.save(JpaFollowRelationEntity.fromDomain(followRelation)).toDomain();
    }

}
