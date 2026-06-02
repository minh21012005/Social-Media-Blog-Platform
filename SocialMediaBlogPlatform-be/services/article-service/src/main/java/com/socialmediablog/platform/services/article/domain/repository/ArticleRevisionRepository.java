package com.socialmediablog.platform.services.article.domain.repository;

import com.socialmediablog.platform.services.article.domain.aggregate.ArticleRevision;
import com.socialmediablog.platform.services.article.domain.vo.ArticleId;

public interface ArticleRevisionRepository {

    int nextVersionFor(ArticleId articleId);

    ArticleRevision save(ArticleRevision revision);
}
