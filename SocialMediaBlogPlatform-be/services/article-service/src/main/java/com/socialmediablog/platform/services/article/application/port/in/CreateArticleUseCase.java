package com.socialmediablog.platform.services.article.application.port.in;

import com.socialmediablog.platform.services.article.application.command.CreateArticleCommand;
import com.socialmediablog.platform.services.article.application.result.ArticleView;

public interface CreateArticleUseCase {

    ArticleView execute(CreateArticleCommand command);
}
