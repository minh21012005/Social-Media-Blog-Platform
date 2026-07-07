package com.socialmediablog.platform.services.comment.application.command;

import java.util.UUID;

public record ClapCommentCommand(UUID commentId, UUID requesterId) {
}
