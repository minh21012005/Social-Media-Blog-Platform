package com.socialmediablog.platform.services.comment.application.command;

import java.util.UUID;

public record UndoClapCommentCommand(UUID commentId, UUID requesterId) {
}
