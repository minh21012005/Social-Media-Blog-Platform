package com.socialmediablog.platform.services.comment.application.command;

import java.util.UUID;

public record ReplyCommentCommand(
        UUID parentCommentId,
        UUID authorId,
        String content
) {
}
