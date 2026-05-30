package com.socialmediablog.platform.services.article.application.port.in;

import com.socialmediablog.platform.services.article.application.command.UploadArticleMediaCommand;
import com.socialmediablog.platform.services.article.application.result.UploadedArticleMedia;

public interface UploadArticleMediaUseCase {

    UploadedArticleMedia execute(UploadArticleMediaCommand command);
}
