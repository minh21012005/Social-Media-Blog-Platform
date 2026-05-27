package com.socialmediablog.platform.services.user.infrastructure.entity;

import com.socialmediablog.platform.services.user.domain.aggregate.RefreshToken;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class JpaRefreshTokenEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected JpaRefreshTokenEntity() {
    }

    private JpaRefreshTokenEntity(
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

    public static JpaRefreshTokenEntity fromDomain(RefreshToken refreshToken) {
        return new JpaRefreshTokenEntity(
                refreshToken.id(),
                refreshToken.userId(),
                refreshToken.tokenHash(),
                refreshToken.expiresAt(),
                refreshToken.revokedAt(),
                refreshToken.createdAt()
        );
    }

    public RefreshToken toDomain() {
        return RefreshToken.restore(id, userId, tokenHash, expiresAt, revokedAt, createdAt);
    }
}
