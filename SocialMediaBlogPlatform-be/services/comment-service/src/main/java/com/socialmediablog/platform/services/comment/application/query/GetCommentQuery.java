package com.socialmediablog.platform.services.comment.application.query;

import java.util.UUID;

public record GetCommentQuery(UUID commentId, UUID currentUserId) {
}