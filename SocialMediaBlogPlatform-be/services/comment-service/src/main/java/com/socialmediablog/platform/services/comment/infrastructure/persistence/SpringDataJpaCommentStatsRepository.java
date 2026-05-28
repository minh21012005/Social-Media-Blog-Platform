package com.socialmediablog.platform.services.comment.infrastructure.persistence;

import com.socialmediablog.platform.services.comment.infrastructure.entity.JpaCommentStatsEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaCommentStatsRepository extends JpaRepository<JpaCommentStatsEntity, UUID> {

    Optional<JpaCommentStatsEntity> findByCommentId(UUID commentId);
}
