package com.socialmediablog.platform.services.comment.application.command;

import java.util.UUID;

public record CreateCommentCommand(
        UUID articleId,
        UUID authorId,
        String content
) {
}
