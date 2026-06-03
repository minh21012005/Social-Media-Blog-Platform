package com.socialmediablog.platform.services.comment.infrastructure.persistence;

import com.socialmediablog.platform.services.comment.infrastructure.entity.JpaCommentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaCommentRepository extends JpaRepository<JpaCommentEntity, UUID> {

    List<JpaCommentEntity> findByArticleId(UUID articleId);

    List<JpaCommentEntity> findByParentCommentIdOrderByCreatedAtAsc(UUID parentCommentId);

    long countByParentCommentIdAndStatusIn(UUID parentCommentId, List<String> statuses);
}
