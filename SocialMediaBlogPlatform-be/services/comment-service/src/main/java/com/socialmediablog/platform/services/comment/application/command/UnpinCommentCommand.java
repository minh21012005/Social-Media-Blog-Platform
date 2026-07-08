package com.socialmediablog.platform.services.comment.application.command;

import java.util.UUID;

public record UnpinCommentCommand(UUID commentId, UUID requesterId) {
}
