package com.socialmediablog.platform.services.comment.application.port.in;

import com.socialmediablog.platform.services.comment.application.query.ListArticleCommentsQuery;
import com.socialmediablog.platform.services.comment.application.result.CommentView;
import java.util.List;

public interface ListArticleCommentsUseCase {

    List<CommentView> execute(ListArticleCommentsQuery query);
}
