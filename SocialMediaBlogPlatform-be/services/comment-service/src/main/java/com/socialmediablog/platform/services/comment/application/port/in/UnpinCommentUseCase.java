package com.socialmediablog.platform.services.comment.application.port.in;

import com.socialmediablog.platform.services.comment.application.command.UnpinCommentCommand;
import com.socialmediablog.platform.services.comment.application.result.CommentView;

public interface UnpinCommentUseCase {
    CommentView execute(UnpinCommentCommand command);
}
