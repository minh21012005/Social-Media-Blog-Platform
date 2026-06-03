package com.socialmediablog.platform.services.comment.application.port.in;

import com.socialmediablog.platform.services.comment.application.command.CreateCommentCommand;
import com.socialmediablog.platform.services.comment.application.result.CommentView;

public interface CreateCommentUseCase {

    CommentView execute(CreateCommentCommand command);
}
