package com.socialmediablog.platform.services.interaction.domain.aggregate;

import com.socialmediablog.platform.services.interaction.domain.vo.ArticleId;
import com.socialmediablog.platform.services.interaction.domain.vo.InteractorId;
import java.time.Instant;

public class Like {

    private final InteractorId userId;
    private final ArticleId articleId;
    private final Instant createdAt;

    private Like(InteractorId userId, ArticleId articleId, Instant createdAt) {
        this.userId = userId;
        this.articleId = articleId;
        this.createdAt = createdAt;
    }

    public static Like create(InteractorId userId, ArticleId articleId, Instant now) {
        return new Like(userId, articleId, now);
    }

    public InteractorId getUserId() {
        return userId;
    }

    public ArticleId getArticleId() {
        return articleId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
