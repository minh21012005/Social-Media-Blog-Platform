package com.socialmediablog.platform.services.article.application.exception;

public class ForbiddenArticleActionException extends RuntimeException {

    public ForbiddenArticleActionException(String message) {
        super(message);
    }
}
