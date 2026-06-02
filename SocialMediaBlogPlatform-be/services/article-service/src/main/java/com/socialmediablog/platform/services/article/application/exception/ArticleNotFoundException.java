package com.socialmediablog.platform.services.article.application.exception;

public class ArticleNotFoundException extends RuntimeException {

    public ArticleNotFoundException(String message) {
        super(message);
    }
}
