package com.socialmediablog.platform.services.article.infrastructure.adapter;

import com.socialmediablog.platform.services.article.domain.aggregate.Article;
import com.socialmediablog.platform.services.article.domain.model.ArticleCategory;
import com.socialmediablog.platform.services.article.domain.model.ArticleStatus;
import com.socialmediablog.platform.services.article.domain.repository.ArticleRepository;
import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import com.socialmediablog.platform.services.article.domain.vo.AuthorId;
import com.socialmediablog.platform.services.article.domain.vo.Slug;
import java.util.List;
import com.socialmediablog.platform.services.article.infrastructure.entity.JpaArticleEntity;
import com.socialmediablog.platform.services.article.infrastructure.persistence.SpringDataJpaArticleRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
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
    public List<Article> findPublished(ArticleCategory category, UUID authorId, String tag, String query, int page, int size) {
        return repository.findPublished(
                        category == null ? null : category.slug(),
                        authorId,
                        normalizeNullable(tag),
                        normalizeNullable(query),
                        PageRequest.of(page, size)
                ).stream()
                .map(JpaArticleEntity::toDomain)
                .toList();
    }

    @Override
    public long countPublished(ArticleCategory category, UUID authorId, String tag, String query) {
        return repository.countPublished(
                category == null ? null : category.slug(),
                authorId,
                normalizeNullable(tag),
                normalizeNullable(query)
        );
    }

    @Override
    public List<Article> findByAuthor(AuthorId authorId, ArticleStatus status, int page, int size) {
        return repository.findByAuthorId(
                        authorId.value(),
                        status == null ? null : status.name(),
                        PageRequest.of(page, size)
                ).stream()
                .map(JpaArticleEntity::toDomain)
                .toList();
    }

    @Override
    public long countByAuthor(AuthorId authorId, ArticleStatus status) {
        return repository.countByAuthorId(authorId.value(), status == null ? null : status.name());
    }

    @Override
    public boolean existsBySlug(Slug slug) {
        return repository.existsBySlug(slug.value());
    }

    @Override
    public boolean existsBySlugAndIdNot(Slug slug, ArticleId id) {
        return repository.existsBySlugAndIdNot(slug.value(), id.value());
    }

    @Override
    public Article save(Article article) {
        return repository.save(JpaArticleEntity.fromDomain(article)).toDomain();
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }
}
