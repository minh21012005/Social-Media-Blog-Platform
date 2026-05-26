package com.socialmediablog.platform.services.user.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import com.socialmediablog.platform.services.user.infrastructure.entity.JpaUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaUserRepository extends JpaRepository<JpaUserEntity, UUID> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<JpaUserEntity> findByUsername(String username);

    Optional<JpaUserEntity> findByEmail(String email);
}
