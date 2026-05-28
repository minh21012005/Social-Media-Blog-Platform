package com.socialmediablog.platform.services.interaction.infrastructure.entity;

import com.socialmediablog.platform.common.web.entity.BaseEntity;
import com.socialmediablog.platform.services.interaction.domain.aggregate.Interaction;
import com.socialmediablog.platform.services.interaction.domain.model.InteractionTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "interactions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_interactions_user_target",
                columnNames = {"user_id", "target_type", "target_id"}
        )
)
public class JpaInteractionEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "clap_count", nullable = false)
    private int clapCount;

    @Column(name = "last_clapped_at", nullable = false)
    private Instant lastClappedAt;

    protected JpaInteractionEntity() {
    }

    private JpaInteractionEntity(
            UUID id,
            UUID userId,
            String targetType,
            UUID targetId,
            int clapCount,
            Instant lastClappedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        super(id, createdAt, updatedAt);
        this.userId = userId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.clapCount = clapCount;
        this.lastClappedAt = lastClappedAt;
    }

    public static JpaInteractionEntity fromDomain(Interaction interaction) {
        return new JpaInteractionEntity(
                interaction.id().value(),
                interaction.userId().value(),
                interaction.targetType().name(),
                interaction.targetId().value(),
                interaction.clapCount(),
                interaction.lastClappedAt(),
                interaction.createdAt(),
                interaction.updatedAt()
        );
    }

    public Interaction toDomain() {
        return Interaction.restore(
                id,
                userId,
                InteractionTargetType.valueOf(targetType),
                targetId,
                clapCount,
                lastClappedAt,
                createdAt,
                updatedAt
        );
    }
}
