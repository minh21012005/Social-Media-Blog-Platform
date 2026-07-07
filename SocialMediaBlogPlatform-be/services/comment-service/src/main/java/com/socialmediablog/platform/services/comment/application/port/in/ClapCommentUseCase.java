package com.socialmediablog.platform.services.comment.application.port.in;

import com.socialmediablog.platform.services.comment.application.command.ClapCommentCommand;

public interface ClapCommentUseCase {
    void execute(ClapCommentCommand command);
}
