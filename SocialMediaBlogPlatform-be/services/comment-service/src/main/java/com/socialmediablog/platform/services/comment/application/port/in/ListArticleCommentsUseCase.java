package com.socialmediablog.platform.services.comment.application.port.in;

import com.socialmediablog.platform.services.comment.application.query.ListArticleCommentsQuery;
import com.socialmediablog.platform.services.comment.application.result.CommentView;
import java.util.List;

import com.socialmediablog.platform.services.comment.application.result.PageResult;

public interface ListArticleCommentsUseCase {

    PageResult<CommentView> execute(ListArticleCommentsQuery query);
}
