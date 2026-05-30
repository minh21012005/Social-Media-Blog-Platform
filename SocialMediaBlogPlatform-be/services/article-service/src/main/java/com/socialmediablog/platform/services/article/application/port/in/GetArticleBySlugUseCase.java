package com.socialmediablog.platform.services.article.application.port.in;

import com.socialmediablog.platform.services.article.application.result.ArticleView;

public interface GetArticleBySlugUseCase {

    ArticleView executeBySlug(String slug);
}
