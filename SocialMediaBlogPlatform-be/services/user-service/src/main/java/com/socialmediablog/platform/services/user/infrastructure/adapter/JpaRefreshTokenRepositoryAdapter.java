package com.socialmediablog.platform.services.user.infrastructure.adapter;

import com.socialmediablog.platform.services.user.application.port.out.RefreshTokenRepository;
import com.socialmediablog.platform.services.user.domain.aggregate.RefreshToken;
import com.socialmediablog.platform.services.user.infrastructure.entity.JpaRefreshTokenEntity;
import com.socialmediablog.platform.services.user.infrastructure.persistence.SpringDataJpaRefreshTokenRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaRefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final SpringDataJpaRefreshTokenRepository repository;

    public JpaRefreshTokenRepositoryAdapter(SpringDataJpaRefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(JpaRefreshTokenEntity::toDomain);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return repository.save(JpaRefreshTokenEntity.fromDomain(refreshToken)).toDomain();
    }

    @Override
    public void revokeActiveTokensByUserId(UUID userId, Instant revokedAt) {
        repository.revokeActiveTokensByUserId(userId, revokedAt);
    }
}
