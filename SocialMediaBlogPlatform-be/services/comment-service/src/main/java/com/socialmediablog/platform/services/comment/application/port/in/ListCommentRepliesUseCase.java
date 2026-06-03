package com.socialmediablog.platform.services.comment.application.port.in;

import com.socialmediablog.platform.services.comment.application.query.ListCommentRepliesQuery;
import com.socialmediablog.platform.services.comment.application.result.CommentView;
import java.util.List;

public interface ListCommentRepliesUseCase {

    List<CommentView> execute(ListCommentRepliesQuery query);
}
