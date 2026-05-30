package com.socialmediablog.platform.services.article.application.port.in;

import com.socialmediablog.platform.services.article.application.command.ListPublishedArticlesCommand;
import com.socialmediablog.platform.services.article.application.result.ArticleView;
import com.socialmediablog.platform.services.article.application.result.PageResult;

public interface ListPublishedArticlesUseCase {

    PageResult<ArticleView> execute(ListPublishedArticlesCommand command);
}
