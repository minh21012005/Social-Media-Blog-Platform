package com.socialmediablog.platform.services.follower.infrastructure.entity;

import com.socialmediablog.platform.common.web.entity.BaseEntity;
import com.socialmediablog.platform.services.follower.domain.aggregate.FollowRelation;
import com.socialmediablog.platform.services.follower.domain.model.FollowRelationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "follow_relations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_follow_relations_pair",
                columnNames = {"follower_id", "followed_user_id"}
        )
)
public class JpaFollowRelationEntity extends BaseEntity {

    @Column(name = "follower_id", nullable = false)
    private UUID followerId;

    @Column(name = "followed_user_id", nullable = false)
    private UUID followedUserId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "followed_at")
    private Instant followedAt;

    @Column(name = "unfollowed_at")
    private Instant unfollowedAt;

    protected JpaFollowRelationEntity() {
    }

    private JpaFollowRelationEntity(
            UUID id,
            UUID followerId,
            UUID followedUserId,
            String status,
            Instant followedAt,
            Instant unfollowedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        super(id, createdAt, updatedAt);
        this.followerId = followerId;
        this.followedUserId = followedUserId;
        this.status = status;
        this.followedAt = followedAt;
        this.unfollowedAt = unfollowedAt;
    }

    public static JpaFollowRelationEntity fromDomain(FollowRelation followRelation) {
        return new JpaFollowRelationEntity(
                followRelation.id().value(),
                followRelation.followerId().value(),
                followRelation.followedUserId().value(),
                followRelation.status().name(),
                followRelation.followedAt(),
                followRelation.unfollowedAt(),
                followRelation.createdAt(),
                followRelation.updatedAt()
        );
    }

    public FollowRelation toDomain() {
        return FollowRelation.restore(
                id,
                followerId,
                followedUserId,
                FollowRelationStatus.valueOf(status),
                followedAt,
                unfollowedAt,
                createdAt,
                updatedAt
        );
    }
}
