package com.socialmediablog.platform.services.article.infrastructure.persistence;

import com.socialmediablog.platform.services.article.infrastructure.entity.JpaArticleRevisionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataJpaArticleRevisionRepository extends JpaRepository<JpaArticleRevisionEntity, UUID> {

    @Query("select coalesce(max(revision.version), 0) from JpaArticleRevisionEntity revision where revision.articleId = :articleId")
    int maxVersionByArticleId(@Param("articleId") UUID articleId);
}
