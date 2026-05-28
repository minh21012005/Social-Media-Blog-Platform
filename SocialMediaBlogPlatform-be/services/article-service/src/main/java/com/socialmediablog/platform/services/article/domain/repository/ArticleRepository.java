package com.socialmediablog.platform.services.article.domain.repository;

import com.socialmediablog.platform.services.article.domain.aggregate.Article;
import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import java.util.Optional;

public interface ArticleRepository {

    Optional<Article> findById(ArticleId id);

    Article save(Article article);
}
