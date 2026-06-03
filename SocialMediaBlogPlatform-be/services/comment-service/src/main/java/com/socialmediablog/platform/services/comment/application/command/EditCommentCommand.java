package com.socialmediablog.platform.services.comment.application.command;

import java.util.UUID;

public record EditCommentCommand(
        UUID commentId,
        UUID requesterId,
        String content
) {
}
