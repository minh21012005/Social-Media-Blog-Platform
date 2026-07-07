package com.socialmediablog.platform.services.comment.api.dto;

import java.util.UUID;

public record CommentCountResponse(UUID articleId, long commentCount) {
}
