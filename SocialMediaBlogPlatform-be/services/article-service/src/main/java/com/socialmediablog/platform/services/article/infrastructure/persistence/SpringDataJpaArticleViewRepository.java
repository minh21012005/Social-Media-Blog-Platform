package com.socialmediablog.platform.services.article.infrastructure.persistence;

import com.socialmediablog.platform.services.article.infrastructure.entity.JpaArticleViewEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaArticleViewRepository extends JpaRepository<JpaArticleViewEntity, UUID> {

    List<JpaArticleViewEntity> findByArticleId(UUID articleId);
}
