package com.socialmediablog.platform.services.comment.application.port.in;

import com.socialmediablog.platform.services.comment.application.command.ReplyCommentCommand;
import com.socialmediablog.platform.services.comment.application.result.CommentView;

public interface ReplyCommentUseCase {

    CommentView execute(ReplyCommentCommand command);
}
