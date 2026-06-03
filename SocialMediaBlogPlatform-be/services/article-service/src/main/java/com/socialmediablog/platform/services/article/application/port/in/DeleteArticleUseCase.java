package com.socialmediablog.platform.services.article.application.port.in;

import com.socialmediablog.platform.services.article.application.command.ArticleActionCommand;

public interface DeleteArticleUseCase {

    void delete(ArticleActionCommand command);
}
