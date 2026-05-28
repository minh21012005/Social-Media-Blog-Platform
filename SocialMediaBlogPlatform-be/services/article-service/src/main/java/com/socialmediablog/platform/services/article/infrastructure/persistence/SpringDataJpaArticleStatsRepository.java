package com.socialmediablog.platform.services.article.infrastructure.persistence;

import com.socialmediablog.platform.services.article.infrastructure.entity.JpaArticleStatsEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaArticleStatsRepository extends JpaRepository<JpaArticleStatsEntity, UUID> {

    Optional<JpaArticleStatsEntity> findByArticleId(UUID articleId);
}
