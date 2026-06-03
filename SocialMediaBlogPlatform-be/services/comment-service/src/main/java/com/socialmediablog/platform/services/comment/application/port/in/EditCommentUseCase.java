package com.socialmediablog.platform.services.comment.application.port.in;

import com.socialmediablog.platform.services.comment.application.command.EditCommentCommand;
import com.socialmediablog.platform.services.comment.application.result.CommentView;

public interface EditCommentUseCase {

    CommentView execute(EditCommentCommand command);
}
