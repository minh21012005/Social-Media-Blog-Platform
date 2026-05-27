package com.socialmediablog.platform.services.user.infrastructure.persistence;

import com.socialmediablog.platform.services.user.infrastructure.entity.JpaRefreshTokenEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataJpaRefreshTokenRepository extends JpaRepository<JpaRefreshTokenEntity, UUID> {

    Optional<JpaRefreshTokenEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            update JpaRefreshTokenEntity token
               set token.revokedAt = :revokedAt
             where token.userId = :userId
               and token.revokedAt is null
            """)
    void revokeActiveTokensByUserId(@Param("userId") UUID userId, @Param("revokedAt") Instant revokedAt);
}
