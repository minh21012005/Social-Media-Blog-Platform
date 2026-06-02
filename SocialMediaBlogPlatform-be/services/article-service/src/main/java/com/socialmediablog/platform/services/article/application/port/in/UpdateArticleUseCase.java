package com.socialmediablog.platform.services.article.application.port.in;

import com.socialmediablog.platform.services.article.application.command.UpdateArticleCommand;
import com.socialmediablog.platform.services.article.application.result.ArticleView;

public interface UpdateArticleUseCase {

    ArticleView execute(UpdateArticleCommand command);
}
