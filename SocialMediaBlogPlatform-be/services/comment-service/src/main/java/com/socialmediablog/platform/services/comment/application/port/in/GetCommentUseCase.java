package com.socialmediablog.platform.services.comment.application.port.in;

import com.socialmediablog.platform.services.comment.application.query.GetCommentQuery;
import com.socialmediablog.platform.services.comment.application.result.CommentView;

public interface GetCommentUseCase {
    CommentView execute(GetCommentQuery query);
}