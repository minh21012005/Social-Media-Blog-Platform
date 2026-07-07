package com.socialmediablog.platform.services.user.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.socialmediablog.platform.services.user.infrastructure.entity.JpaUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaUserRepository extends JpaRepository<JpaUserEntity, UUID> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<JpaUserEntity> findByUsername(String username);

    Optional<JpaUserEntity> findByEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM JpaUserEntity u WHERE u.status = 'ACTIVE' AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<JpaUserEntity> searchActiveUsers(@org.springframework.data.repository.query.Param("query") String query);
}
