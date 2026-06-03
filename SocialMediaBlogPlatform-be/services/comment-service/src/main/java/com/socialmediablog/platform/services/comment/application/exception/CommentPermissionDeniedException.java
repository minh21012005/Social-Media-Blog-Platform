package com.socialmediablog.platform.services.comment.application.exception;

public class CommentPermissionDeniedException extends RuntimeException {

    public CommentPermissionDeniedException() {
        super("You are not allowed to modify this comment");
    }
}
