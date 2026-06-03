package com.socialmediablog.platform.services.comment.application.exception;

public class CommentPermissionDeniedException extends RuntimeException {

    public CommentPermissionDeniedException() {
        super("Only the comment author can edit this comment");
    }
}
