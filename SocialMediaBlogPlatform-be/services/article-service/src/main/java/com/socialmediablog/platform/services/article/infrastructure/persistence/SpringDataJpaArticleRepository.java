package com.socialmediablog.platform.services.article.infrastructure.persistence;

import com.socialmediablog.platform.services.article.infrastructure.entity.JpaArticleEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaArticleRepository extends JpaRepository<JpaArticleEntity, UUID> {

    Optional<JpaArticleEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
