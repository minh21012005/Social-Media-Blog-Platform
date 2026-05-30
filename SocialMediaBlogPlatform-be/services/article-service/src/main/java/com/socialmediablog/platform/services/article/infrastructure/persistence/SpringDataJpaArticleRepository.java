package com.socialmediablog.platform.services.article.infrastructure.persistence;

import com.socialmediablog.platform.services.article.infrastructure.entity.JpaArticleEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SpringDataJpaArticleRepository extends JpaRepository<JpaArticleEntity, UUID>, JpaSpecificationExecutor<JpaArticleEntity> {

    Optional<JpaArticleEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);
}
