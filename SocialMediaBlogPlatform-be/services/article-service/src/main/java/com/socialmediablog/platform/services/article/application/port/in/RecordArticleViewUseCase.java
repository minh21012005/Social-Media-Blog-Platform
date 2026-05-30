package com.socialmediablog.platform.services.article.application.port.in;

import com.socialmediablog.platform.services.article.application.command.RecordArticleViewCommand;

public interface RecordArticleViewUseCase {

    void execute(RecordArticleViewCommand command);
}
