package com.socialmediablog.platform.services.comment.infrastructure.persistence;

import com.socialmediablog.platform.services.comment.infrastructure.entity.JpaCommentClapEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataJpaCommentClapRepository extends JpaRepository<JpaCommentClapEntity, UUID> {
    
    List<JpaCommentClapEntity> findByCommentIdAndUserId(UUID commentId, UUID userId);

    @Modifying
    @Query("DELETE FROM JpaCommentClapEntity c WHERE c.commentId = :commentId AND c.userId = :userId")
    void deleteByCommentIdAndUserId(@Param("commentId") UUID commentId, @Param("userId") UUID userId);

    @Query("SELECT DISTINCT c.commentId FROM JpaCommentClapEntity c WHERE c.userId = :userId AND c.commentId IN :commentIds")
    List<UUID> findClappedCommentIds(@Param("userId") UUID userId, @Param("commentIds") List<UUID> commentIds);
}
