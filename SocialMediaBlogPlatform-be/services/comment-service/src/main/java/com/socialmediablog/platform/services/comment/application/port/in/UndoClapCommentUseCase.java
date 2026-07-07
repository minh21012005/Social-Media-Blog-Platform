package com.socialmediablog.platform.services.comment.application.port.in;

import com.socialmediablog.platform.services.comment.application.command.UndoClapCommentCommand;

public interface UndoClapCommentUseCase {
    long execute(UndoClapCommentCommand command);
}
