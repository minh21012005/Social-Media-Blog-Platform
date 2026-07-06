package com.socialmediablog.platform.services.follower.infrastructure.entity;

import com.socialmediablog.platform.common.web.entity.BaseEntity;
import com.socialmediablog.platform.services.follower.domain.aggregate.Mute;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "mutes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_mutes_pair",
                columnNames = {"muter_id", "muted_user_id"}
        )
)
public class JpaMuteEntity extends BaseEntity {

    @Column(name = "muter_id", nullable = false)
    private UUID muterId;

    @Column(name = "muted_user_id", nullable = false)
    private UUID mutedUserId;

    protected JpaMuteEntity() {
    }

    private JpaMuteEntity(UUID id, UUID muterId, UUID mutedUserId, Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.muterId = muterId;
        this.mutedUserId = mutedUserId;
    }

    public static JpaMuteEntity fromDomain(Mute mute) {
        return new JpaMuteEntity(
                mute.id().value(),
                mute.muterId(),
                mute.mutedUserId(),
                mute.createdAt(),
                mute.updatedAt()
        );
    }

    public Mute toDomain() {
        return Mute.restore(id, muterId, mutedUserId, createdAt, updatedAt);
    }
}
