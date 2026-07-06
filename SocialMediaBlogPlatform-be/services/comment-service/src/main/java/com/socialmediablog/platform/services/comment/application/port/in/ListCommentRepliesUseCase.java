package com.socialmediablog.platform.services.comment.application.port.in;

import com.socialmediablog.platform.services.comment.application.query.ListCommentRepliesQuery;
import com.socialmediablog.platform.services.comment.application.result.CommentView;
import java.util.List;

import com.socialmediablog.platform.services.comment.application.result.PageResult;

public interface ListCommentRepliesUseCase {

    PageResult<CommentView> execute(ListCommentRepliesQuery query);
}
