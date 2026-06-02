package com.socialmediablog.platform.services.article.application.port.in;

import com.socialmediablog.platform.services.article.application.command.CurateArticleCommand;
import com.socialmediablog.platform.services.article.application.result.ArticleView;

public interface CurateArticleUseCase {

    ArticleView curate(CurateArticleCommand command);
}
