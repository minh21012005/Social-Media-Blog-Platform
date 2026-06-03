package com.socialmediablog.platform.services.comment.application.command;

import java.util.UUID;

public record DeleteCommentCommand(
        UUID commentId,
        UUID requesterId
) {
}
