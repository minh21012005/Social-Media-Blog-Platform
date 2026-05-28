package com.socialmediablog.platform.services.article.domain.aggregate;

import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import java.util.UUID;

public class Article {

    private final ArticleId id;

    private Article(ArticleId id) {
        this.id = id;
    }

    public static Article restore(UUID id) {
        return new Article(ArticleId.of(id));
    }

    public ArticleId id() {
        return id;
    }
}
