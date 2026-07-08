package com.socialmediablog.platform.services.interaction.infrastructure.persistence;

import com.socialmediablog.platform.services.interaction.infrastructure.entity.JpaInteractionEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaInteractionRepository extends JpaRepository<JpaInteractionEntity, UUID> {

    Optional<JpaInteractionEntity> findByUserIdAndTargetTypeAndTargetId(UUID userId, String targetType, UUID targetId);

    boolean existsByUserIdAndTargetTypeAndTargetId(UUID userId, String targetType, UUID targetId);

    long countByTargetTypeAndTargetId(String targetType, UUID targetId);
}
