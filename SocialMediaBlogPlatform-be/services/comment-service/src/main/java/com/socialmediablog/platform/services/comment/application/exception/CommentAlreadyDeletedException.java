package com.socialmediablog.platform.services.comment.application.exception;

public class CommentAlreadyDeletedException extends RuntimeException {

    public CommentAlreadyDeletedException() {
        super("Deleted comment cannot be edited");
    }
}
