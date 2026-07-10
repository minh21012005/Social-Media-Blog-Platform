package com.socialmediablog.platform.services.interaction.infrastructure.persistence;

import com.socialmediablog.platform.services.interaction.infrastructure.entity.JpaInteractionEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataJpaInteractionRepository extends JpaRepository<JpaInteractionEntity, UUID> {

    Optional<JpaInteractionEntity> findByUserIdAndTargetTypeAndTargetId(UUID userId, String targetType, UUID targetId);

    boolean existsByUserIdAndTargetTypeAndTargetId(UUID userId, String targetType, UUID targetId);

    @Query("SELECT COALESCE(SUM(i.clapCount), 0) FROM JpaInteractionEntity i WHERE i.targetType = :targetType AND i.targetId = :targetId")
    long sumClapCountByTargetTypeAndTargetId(@Param("targetType") String targetType, @Param("targetId") UUID targetId);
}
