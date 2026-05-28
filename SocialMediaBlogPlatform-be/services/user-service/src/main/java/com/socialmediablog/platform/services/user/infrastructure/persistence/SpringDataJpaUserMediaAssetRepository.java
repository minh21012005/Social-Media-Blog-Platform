package com.socialmediablog.platform.services.user.infrastructure.persistence;

import com.socialmediablog.platform.services.user.infrastructure.entity.JpaUserMediaAssetEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaUserMediaAssetRepository extends JpaRepository<JpaUserMediaAssetEntity, UUID> {

    List<JpaUserMediaAssetEntity> findByUserId(UUID userId);
}
