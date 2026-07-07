package com.socialmediablog.platform.services.follower.infrastructure.persistence;

import com.socialmediablog.platform.services.follower.infrastructure.entity.JpaMuteEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataJpaMuteRepository extends JpaRepository<JpaMuteEntity, UUID> {
    Optional<JpaMuteEntity> findByMuterIdAndMutedUserId(UUID muterId, UUID mutedUserId);
    List<JpaMuteEntity> findByMuterId(UUID muterId, Pageable pageable);
    long countByMuterId(UUID muterId);

    @Query("SELECT e.mutedUserId FROM JpaMuteEntity e WHERE e.muterId = :muterId")
    List<UUID> findMutedUserIdsByMuterId(@Param("muterId") UUID muterId);
}
