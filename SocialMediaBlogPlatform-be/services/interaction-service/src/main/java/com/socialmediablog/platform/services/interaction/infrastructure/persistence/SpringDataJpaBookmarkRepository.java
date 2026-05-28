package com.socialmediablog.platform.services.interaction.infrastructure.persistence;

import com.socialmediablog.platform.services.interaction.infrastructure.entity.JpaBookmarkEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaBookmarkRepository extends JpaRepository<JpaBookmarkEntity, UUID> {

    Optional<JpaBookmarkEntity> findByUserIdAndArticleId(UUID userId, UUID articleId);

    List<JpaBookmarkEntity> findByUserId(UUID userId);
}
