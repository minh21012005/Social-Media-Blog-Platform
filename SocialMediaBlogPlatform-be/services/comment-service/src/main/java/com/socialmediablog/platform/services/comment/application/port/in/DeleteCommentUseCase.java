package com.socialmediablog.platform.services.comment.application.port.in;

import com.socialmediablog.platform.services.comment.application.command.DeleteCommentCommand;

public interface DeleteCommentUseCase {

    void execute(DeleteCommentCommand command);
}
