package com.socialmediablog.platform.services.interaction.domain.repository;

import com.socialmediablog.platform.services.interaction.domain.aggregate.Bookmark;
import com.socialmediablog.platform.services.interaction.domain.vo.ArticleId;
import com.socialmediablog.platform.services.interaction.domain.vo.BookmarkId;
import com.socialmediablog.platform.services.interaction.domain.vo.InteractorId;
import java.util.List;
import java.util.Optional;

public interface BookmarkRepository {

    Optional<Bookmark> findById(BookmarkId id);

    Optional<Bookmark> findByUserIdAndArticleId(InteractorId userId, ArticleId articleId);

    List<Bookmark> findByUserId(InteractorId userId);

    Bookmark save(Bookmark bookmark);
}
