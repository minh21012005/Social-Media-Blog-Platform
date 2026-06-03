package com.socialmediablog.platform.services.comment.application.exception;

import java.util.UUID;

public class CommentNotFoundException extends RuntimeException {

    public CommentNotFoundException(UUID commentId) {
        super("Comment was not found: " + commentId);
    }
}
