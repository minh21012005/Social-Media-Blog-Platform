package com.socialmediablog.platform.services.article.application.port.in;

import com.socialmediablog.platform.services.article.application.result.ArticleView;
import java.util.UUID;

public interface GetMyArticleUseCase {
    ArticleView executeMyArticle(UUID articleId, UUID requesterId);
}
