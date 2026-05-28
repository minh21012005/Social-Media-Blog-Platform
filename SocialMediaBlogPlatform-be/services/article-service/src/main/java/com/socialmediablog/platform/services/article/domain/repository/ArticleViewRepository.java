package com.socialmediablog.platform.services.article.domain.repository;

import com.socialmediablog.platform.services.article.domain.aggregate.ArticleView;
import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import java.util.List;

public interface ArticleViewRepository {

    List<ArticleView> findByArticleId(ArticleId articleId);

    ArticleView save(ArticleView articleView);
}
