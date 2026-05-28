package com.socialmediablog.platform.services.article.infrastructure.persistence;

import com.socialmediablog.platform.services.article.infrastructure.entity.JpaArticleMediaAssetEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaArticleMediaAssetRepository extends JpaRepository<JpaArticleMediaAssetEntity, UUID> {

    List<JpaArticleMediaAssetEntity> findByArticleId(UUID articleId);
}
