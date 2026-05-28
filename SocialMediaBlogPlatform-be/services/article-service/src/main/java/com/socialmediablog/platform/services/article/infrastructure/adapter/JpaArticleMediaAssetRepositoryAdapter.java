package com.socialmediablog.platform.services.article.infrastructure.adapter;

import com.socialmediablog.platform.services.article.domain.aggregate.ArticleMediaAsset;
import com.socialmediablog.platform.services.article.domain.repository.ArticleMediaAssetRepository;
import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import com.socialmediablog.platform.services.article.domain.vo.ArticleMediaAssetId;
import com.socialmediablog.platform.services.article.infrastructure.entity.JpaArticleMediaAssetEntity;
import com.socialmediablog.platform.services.article.infrastructure.persistence.SpringDataJpaArticleMediaAssetRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaArticleMediaAssetRepositoryAdapter implements ArticleMediaAssetRepository {

    private final SpringDataJpaArticleMediaAssetRepository repository;

    public JpaArticleMediaAssetRepositoryAdapter(SpringDataJpaArticleMediaAssetRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ArticleMediaAsset> findById(ArticleMediaAssetId id) {
        return repository.findById(id.value()).map(JpaArticleMediaAssetEntity::toDomain);
    }

    @Override
    public List<ArticleMediaAsset> findByArticleId(ArticleId articleId) {
        return repository.findByArticleId(articleId.value()).stream()
                .map(JpaArticleMediaAssetEntity::toDomain)
                .toList();
    }

    @Override
    public ArticleMediaAsset save(ArticleMediaAsset mediaAsset) {
        return repository.save(JpaArticleMediaAssetEntity.fromDomain(mediaAsset)).toDomain();
    }
}
