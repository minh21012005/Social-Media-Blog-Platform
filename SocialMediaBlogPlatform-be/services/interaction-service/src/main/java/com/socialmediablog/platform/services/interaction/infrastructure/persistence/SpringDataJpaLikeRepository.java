package com.socialmediablog.platform.services.interaction.infrastructure.persistence;

import com.socialmediablog.platform.services.interaction.infrastructure.persistence.entity.LikeEntity;
import com.socialmediablog.platform.services.interaction.infrastructure.persistence.entity.LikeEntityId;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaLikeRepository extends JpaRepository<LikeEntity, LikeEntityId> {
    long countByIdArticleId(UUID articleId);
}
