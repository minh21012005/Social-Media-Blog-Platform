package com.socialmediablog.platform.services.interaction.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class LikeEntityId implements Serializable {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "article_id")
    private UUID articleId;

    public LikeEntityId() {
    }

    public LikeEntityId(UUID userId, UUID articleId) {
        this.userId = userId;
        this.articleId = articleId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getArticleId() {
        return articleId;
    }

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LikeEntityId that = (LikeEntityId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(articleId, that.articleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, articleId);
    }
}
