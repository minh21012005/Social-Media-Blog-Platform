package com.socialmediablog.platform.services.interaction.domain.aggregate;

import com.socialmediablog.platform.services.interaction.domain.model.InteractionTargetType;
import com.socialmediablog.platform.services.interaction.domain.vo.InteractorId;
import com.socialmediablog.platform.services.interaction.domain.vo.InteractionId;
import com.socialmediablog.platform.services.interaction.domain.vo.TargetId;
import java.time.Instant;
import java.util.UUID;

public class Interaction {

    private final InteractionId id;
    private final InteractorId userId;
    private final InteractionTargetType targetType;
    private final TargetId targetId;
    private final int clapCount;
    private final Instant lastClappedAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Interaction(
            InteractionId id,
            InteractorId userId,
            InteractionTargetType targetType,
            TargetId targetId,
            int clapCount,
            Instant lastClappedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.clapCount = validateClapCount(clapCount);
        this.lastClappedAt = lastClappedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Interaction record(
            InteractorId userId,
            InteractionTargetType targetType,
            TargetId targetId,
            int clapCount,
            Instant now
    ) {
        return new Interaction(
                InteractionId.of(UUID.randomUUID()),
                userId,
                targetType,
                targetId,
                clapCount,
                now,
                now,
                now
        );
    }

    public static Interaction restore(
            UUID id,
            UUID userId,
            InteractionTargetType targetType,
            UUID targetId,
            int clapCount,
            Instant lastClappedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Interaction(
                InteractionId.of(id),
                InteractorId.of(userId),
                targetType,
                TargetId.of(targetId),
                clapCount,
                lastClappedAt,
                createdAt,
                updatedAt
        );
    }

    public Interaction clap(Instant now) {
        return new Interaction(
                id,
                userId,
                targetType,
                targetId,
                clapCount + 1,
                now,
                createdAt,
                now
        );
    }

    private static int validateClapCount(int clapCount) {
        if (clapCount < 1) {
            throw new IllegalArgumentException("Clap count must be at least 1");
        }
        return clapCount;
    }

    public InteractionId id() {
        return id;
    }

    public InteractorId userId() {
        return userId;
    }

    public InteractionTargetType targetType() {
        return targetType;
    }

    public TargetId targetId() {
        return targetId;
    }

    public int clapCount() {
        return clapCount;
    }

    public Instant lastClappedAt() {
        return lastClappedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
