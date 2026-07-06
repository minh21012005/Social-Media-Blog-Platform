package com.socialmediablog.platform.services.comment.application.port.in;

import com.socialmediablog.platform.services.comment.application.query.CountArticleCommentsQuery;

public interface CountArticleCommentsUseCase {

    long execute(CountArticleCommentsQuery query);
}
