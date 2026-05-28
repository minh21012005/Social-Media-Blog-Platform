package com.socialmediablog.platform.services.article.infrastructure.adapter;

import com.socialmediablog.platform.services.article.domain.aggregate.Article;
import com.socialmediablog.platform.services.article.domain.repository.ArticleRepository;
import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import com.socialmediablog.platform.services.article.domain.vo.Slug;
import com.socialmediablog.platform.services.article.infrastructure.entity.JpaArticleEntity;
import com.socialmediablog.platform.services.article.infrastructure.persistence.SpringDataJpaArticleRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaArticleRepositoryAdapter implements ArticleRepository {

    private final SpringDataJpaArticleRepository repository;

    public JpaArticleRepositoryAdapter(SpringDataJpaArticleRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Article> findById(ArticleId id) {
        return repository.findById(id.value()).map(JpaArticleEntity::toDomain);
    }

    @Override
    public Optional<Article> findBySlug(Slug slug) {
        return repository.findBySlug(slug.value()).map(JpaArticleEntity::toDomain);
    }

    @Override
    public boolean existsBySlug(Slug slug) {
        return repository.existsBySlug(slug.value());
    }

    @Override
    public Article save(Article article) {
        return repository.save(JpaArticleEntity.fromDomain(article)).toDomain();
    }
}
