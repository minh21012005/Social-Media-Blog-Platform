package com.socialmediablog.platform.services.comment.application.command;

import java.util.UUID;

public record PinCommentCommand(UUID commentId, UUID requesterId) {
}
