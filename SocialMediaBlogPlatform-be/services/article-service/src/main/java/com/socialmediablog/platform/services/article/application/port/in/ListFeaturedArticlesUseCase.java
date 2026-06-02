package com.socialmediablog.platform.services.article.application.port.in;

import com.socialmediablog.platform.services.article.application.command.ListCuratedArticlesCommand;
import com.socialmediablog.platform.services.article.application.result.ArticleView;
import java.util.List;

public interface ListFeaturedArticlesUseCase {

    List<ArticleView> executeFeatured(ListCuratedArticlesCommand command);
}
