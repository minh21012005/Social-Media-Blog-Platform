package com.socialmediablog.platform.services.article.application.port.out;

import java.util.UUID;

public interface ArticleLikeCountProvider {

    long countLikes(UUID articleId);
}

