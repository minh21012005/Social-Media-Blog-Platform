package com.socialmediablog.platform.services.follower.domain.aggregate;

import com.socialmediablog.platform.services.follower.domain.vo.MuteId;
import java.time.Instant;
import java.util.UUID;

public class Mute {
    private final MuteId id;
    private final UUID muterId;
    private final UUID mutedUserId;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Mute(MuteId id, UUID muterId, UUID mutedUserId, Instant createdAt, Instant updatedAt) {
        if (muterId != null && mutedUserId != null && muterId.equals(mutedUserId)) {
            throw new IllegalArgumentException("User cannot mute themselves");
        }
        this.id = id;
        this.muterId = muterId;
        this.mutedUserId = mutedUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Mute mute(UUID muterId, UUID mutedUserId, Instant now) {
        return new Mute(MuteId.of(UUID.randomUUID()), muterId, mutedUserId, now, now);
    }

    public static Mute restore(UUID id, UUID muterId, UUID mutedUserId, Instant createdAt, Instant updatedAt) {
        return new Mute(MuteId.of(id), muterId, mutedUserId, createdAt, updatedAt);
    }

    public MuteId id() { return id; }
    public UUID muterId() { return muterId; }
    public UUID mutedUserId() { return mutedUserId; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
