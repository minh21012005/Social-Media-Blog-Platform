package com.socialmediablog.platform.services.user.application.port.out;

import com.socialmediablog.platform.services.user.domain.aggregate.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    RefreshToken save(RefreshToken refreshToken);

    void revokeActiveTokensByUserId(UUID userId, Instant revokedAt);
}
