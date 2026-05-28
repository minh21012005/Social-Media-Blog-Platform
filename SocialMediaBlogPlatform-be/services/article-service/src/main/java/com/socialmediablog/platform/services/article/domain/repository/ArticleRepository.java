package com.socialmediablog.platform.services.article.domain.repository;

import com.socialmediablog.platform.services.article.domain.aggregate.Article;
import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import com.socialmediablog.platform.services.article.domain.vo.Slug;
import java.util.Optional;

public interface ArticleRepository {

    Optional<Article> findById(ArticleId id);

    Optional<Article> findBySlug(Slug slug);

    boolean existsBySlug(Slug slug);

    Article save(Article article);
}
