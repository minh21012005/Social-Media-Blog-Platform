package com.socialmediablog.platform.services.user.domain.aggregate;

import java.time.Instant;
import java.util.UUID;

public class RefreshToken {

    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final Instant expiresAt;
    private final Instant revokedAt;
    private final Instant createdAt;

    private RefreshToken(
            UUID id,
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            Instant revokedAt,
            Instant createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.createdAt = createdAt;
    }

    public static RefreshToken issue(UUID userId, String tokenHash, Instant expiresAt, Instant now) {
        return new RefreshToken(UUID.randomUUID(), userId, tokenHash, expiresAt, null, now);
    }

    public static RefreshToken restore(
            UUID id,
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            Instant revokedAt,
            Instant createdAt
    ) {
        return new RefreshToken(id, userId, tokenHash, expiresAt, revokedAt, createdAt);
    }

    public RefreshToken revoke(Instant revokedAt) {
        if (this.revokedAt != null) {
            return this;
        }
        return new RefreshToken(id, userId, tokenHash, expiresAt, revokedAt, createdAt);
    }

    public boolean isUsableAt(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }

    public UUID id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public String tokenHash() {
        return tokenHash;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant revokedAt() {
        return revokedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
