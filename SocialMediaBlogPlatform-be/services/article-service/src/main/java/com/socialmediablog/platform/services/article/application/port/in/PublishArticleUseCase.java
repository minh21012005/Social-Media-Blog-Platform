package com.socialmediablog.platform.services.article.application.port.in;

import com.socialmediablog.platform.services.article.application.command.ArticleActionCommand;
import com.socialmediablog.platform.services.article.application.result.ArticleView;

public interface PublishArticleUseCase {

    ArticleView publish(ArticleActionCommand command);
}
