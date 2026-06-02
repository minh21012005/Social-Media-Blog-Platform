package com.socialmediablog.platform.services.article.domain.repository;

import com.socialmediablog.platform.services.article.domain.aggregate.Article;
import com.socialmediablog.platform.services.article.domain.model.ArticleCategory;
import com.socialmediablog.platform.services.article.domain.model.ArticleStatus;
import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import com.socialmediablog.platform.services.article.domain.vo.AuthorId;
import com.socialmediablog.platform.services.article.domain.vo.Slug;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArticleRepository {

    Optional<Article> findById(ArticleId id);

    Optional<Article> findBySlug(Slug slug);

    List<Article> findPublished(ArticleCategory category, UUID authorId, String tag, String query, int page, int size);

    long countPublished(ArticleCategory category, UUID authorId, String tag, String query);

    List<Article> findByAuthor(AuthorId authorId, ArticleStatus status, int page, int size);

    long countByAuthor(AuthorId authorId, ArticleStatus status);

    boolean existsBySlug(Slug slug);

    boolean existsBySlugAndIdNot(Slug slug, ArticleId id);

    Article save(Article article);
}
