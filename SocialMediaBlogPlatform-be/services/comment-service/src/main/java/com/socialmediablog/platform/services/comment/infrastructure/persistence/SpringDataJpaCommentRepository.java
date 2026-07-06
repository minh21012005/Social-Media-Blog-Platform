package com.socialmediablog.platform.services.comment.infrastructure.persistence;

import com.socialmediablog.platform.services.comment.infrastructure.entity.JpaCommentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SpringDataJpaCommentRepository extends JpaRepository<JpaCommentEntity, UUID> {

        @Query("SELECT c FROM JpaCommentEntity c WHERE c.articleId = :articleId AND c.parentCommentId IS NULL")
        Page<JpaCommentEntity> findRootCommentsByArticleId(@Param("articleId") UUID articleId, Pageable pageable);

        @Query("SELECT COUNT(c) FROM JpaCommentEntity c WHERE c.articleId = :articleId AND c.parentCommentId IS NULL")
        long countRootCommentsByArticleId(@Param("articleId") UUID articleId);

        Page<JpaCommentEntity> findByParentCommentIdOrderByCreatedAtAsc(UUID parentCommentId, Pageable pageable);

        long countByParentCommentIdAndStatusIn(UUID parentCommentId, List<String> statuses);

        long countByArticleIdAndStatusIn(UUID articleId, List<String> statuses);
}
