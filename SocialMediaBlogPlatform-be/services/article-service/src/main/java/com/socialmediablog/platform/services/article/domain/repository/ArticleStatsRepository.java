package com.socialmediablog.platform.services.article.domain.repository;

import com.socialmediablog.platform.services.article.domain.aggregate.ArticleStats;
import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import java.util.Optional;

public interface ArticleStatsRepository {

    Optional<ArticleStats> findByArticleId(ArticleId articleId);

    ArticleStats save(ArticleStats articleStats);
}
