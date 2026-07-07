package com.socialmediablog.platform.services.article.infrastructure.adapter;

import com.socialmediablog.platform.services.article.domain.aggregate.Article;
import com.socialmediablog.platform.services.article.domain.model.ArticleCategory;
import com.socialmediablog.platform.services.article.domain.model.ArticleStatus;
import com.socialmediablog.platform.services.article.domain.repository.ArticleRepository;
import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import com.socialmediablog.platform.services.article.domain.vo.AuthorId;
import com.socialmediablog.platform.services.article.domain.vo.Slug;
import com.socialmediablog.platform.services.article.infrastructure.entity.JpaArticleEntity;
import com.socialmediablog.platform.services.article.infrastructure.persistence.SpringDataJpaArticleRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JpaArticleRepositoryAdapter implements ArticleRepository {

    private static final Logger log = LoggerFactory.getLogger(JpaArticleRepositoryAdapter.class);

    private final SpringDataJpaArticleRepository repository;
    private final RedisTemplate<String, String> redisTemplate;

    public JpaArticleRepositoryAdapter(
            SpringDataJpaArticleRepository repository,
            RedisTemplate<String, String> redisTemplate) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
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
    public List<Article> findPublished(ArticleCategory category, UUID authorId, String tag, String query, String sort,
            int page, int size) {
        return repository.findPublished(
                category == null ? null : category.slug(),
                authorId,
                normalizeNullable(tag),
                normalizeNullable(query),
                normalizeSort(sort),
                PageRequest.of(page, size))
                .stream()
                .map(JpaArticleEntity::toDomain)
                .toList();
    }

    @Override
    public long countPublished(ArticleCategory category, UUID authorId, String tag, String query) {
        return repository.count(publishedSpecification(category, authorId, tag, query));
    }

    @Override
    public List<Article> findFeatured(int size) {
        return repository.findFeatured(PageRequest.of(0, Math.max(size, 1)))
                .stream()
                .map(JpaArticleEntity::toDomain)
                .toList();
    }

    @Override
    public List<Article> findEditorPicks(int size) {
        return repository.findEditorPicks(PageRequest.of(0, Math.max(size, 1)))
                .stream()
                .map(JpaArticleEntity::toDomain)
                .toList();
    }

    @Override
    public List<Article> findByAuthor(AuthorId authorId, ArticleStatus status, int page, int size) {
        return repository.findAll(
                authorSpecification(authorId, status),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt")))
                .stream()
                .map(JpaArticleEntity::toDomain)
                .toList();
    }

    @Override
    public long countByAuthor(AuthorId authorId, ArticleStatus status) {
        return repository.count(authorSpecification(authorId, status));
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

    @Override
    public List<Article> findTrending(int size) {
        int limit = Math.max(size, 1);
        try {
            // Read the ordered ID list from Redis
            List<String> rawIds = redisTemplate.opsForList().range(TrendingScoreJob.TRENDING_IDS_KEY, 0, limit - 1);
            if (rawIds != null && !rawIds.isEmpty()) {
                List<UUID> ids = rawIds.stream().map(UUID::fromString).toList();
                Map<UUID, Article> byId = repository.findAllById(ids).stream()
                        .collect(java.util.stream.Collectors.toMap(
                                e -> e.id(),
                                JpaArticleEntity::toDomain,
                                (a, b) -> a,
                                LinkedHashMap::new));
                // Return in Redis score order, skipping any IDs that no longer exist
                return ids.stream()
                        .filter(byId::containsKey)
                        .map(byId::get)
                        .limit(limit)
                        .toList();
            }
        } catch (Exception ex) {
            log.warn("[findTrending] Could not read from Redis, falling back to latest: {}", ex.getMessage());
        }
        // Fallback: return latest published articles when Redis is unavailable
        return repository.findPublished(null, null, null, null, "latest", PageRequest.of(0, limit))
                .stream()
                .map(JpaArticleEntity::toDomain)
                .toList();
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private String normalizeSort(String sort) {
        String normalized = normalizeNullable(sort);
        if ("views".equals(normalized) || "popular".equals(normalized)) {
            return normalized;
        }
        return "latest";
    }

    private Specification<JpaArticleEntity> publishedSpecification(
            ArticleCategory category,
            UUID authorId,
            String tag,
            String query) {
        String normalizedTag = normalizeNullable(tag);
        String normalizedQuery = normalizeNullable(query);
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("status"), ArticleStatus.PUBLISHED.name()));

            if (category != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category.slug()));
            }
            if (authorId != null) {
                predicates.add(criteriaBuilder.equal(root.get("authorId"), authorId));
            }
            if (normalizedTag != null) {
                Join<JpaArticleEntity, String> tags = root.join("tags");
                predicates.add(criteriaBuilder.equal(tags, normalizedTag));
                criteriaQuery.distinct(true);
            }
            if (normalizedQuery != null) {
                String pattern = "%" + normalizedQuery + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("summary")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), pattern)));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<JpaArticleEntity> authorSpecification(AuthorId authorId, ArticleStatus status) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("authorId"), authorId.value()));
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status.name()));
            } else {
                predicates.add(criteriaBuilder.notEqual(root.get("status"), ArticleStatus.DELETED.name()));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
