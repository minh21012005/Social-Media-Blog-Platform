package com.socialmediablog.platform.services.article.application.exception;

public class DuplicateArticleSlugException extends RuntimeException {

    public DuplicateArticleSlugException(String message) {
        super(message);
    }
}
