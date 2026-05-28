package com.socialmediablog.platform.services.follower.infrastructure.adapter;

import com.socialmediablog.platform.services.follower.domain.aggregate.FollowRelation;
import com.socialmediablog.platform.services.follower.domain.repository.FollowRelationRepository;
import com.socialmediablog.platform.services.follower.domain.vo.FollowedUserId;
import com.socialmediablog.platform.services.follower.domain.vo.FollowRelationId;
import com.socialmediablog.platform.services.follower.domain.vo.FollowerId;
import com.socialmediablog.platform.services.follower.infrastructure.entity.JpaFollowRelationEntity;
import com.socialmediablog.platform.services.follower.infrastructure.persistence.SpringDataJpaFollowRelationRepository;
import java.util.Optional;
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
    public FollowRelation save(FollowRelation followRelation) {
        return repository.save(JpaFollowRelationEntity.fromDomain(followRelation)).toDomain();
    }
}
