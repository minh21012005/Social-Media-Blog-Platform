package com.socialmediablog.platform.services.comment.application.port.in;

import com.socialmediablog.platform.services.comment.application.command.PinCommentCommand;
import com.socialmediablog.platform.services.comment.application.result.CommentView;

public interface PinCommentUseCase {
    CommentView execute(PinCommentCommand command);
}
