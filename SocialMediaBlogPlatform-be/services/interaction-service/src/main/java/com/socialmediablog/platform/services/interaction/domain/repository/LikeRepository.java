package com.socialmediablog.platform.services.interaction.domain.repository;

import com.socialmediablog.platform.services.interaction.domain.aggregate.Like;
import com.socialmediablog.platform.services.interaction.domain.vo.ArticleId;
import com.socialmediablog.platform.services.interaction.domain.vo.InteractorId;
import java.util.Optional;

public interface LikeRepository {
    void save(Like like);
    void delete(Like like);
    Optional<Like> findById(InteractorId userId, ArticleId articleId);
    long countByArticle(ArticleId articleId);
}
