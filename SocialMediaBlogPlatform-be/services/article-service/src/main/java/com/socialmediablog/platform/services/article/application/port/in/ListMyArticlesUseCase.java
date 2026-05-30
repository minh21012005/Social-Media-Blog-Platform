package com.socialmediablog.platform.services.article.application.port.in;

import com.socialmediablog.platform.services.article.application.command.ListMyArticlesCommand;
import com.socialmediablog.platform.services.article.application.result.ArticleView;
import com.socialmediablog.platform.services.article.application.result.PageResult;

public interface ListMyArticlesUseCase {

    PageResult<ArticleView> execute(ListMyArticlesCommand command);
}
