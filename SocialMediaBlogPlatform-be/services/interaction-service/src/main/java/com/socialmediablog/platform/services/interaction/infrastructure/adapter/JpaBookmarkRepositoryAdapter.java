package com.socialmediablog.platform.services.interaction.infrastructure.adapter;

import com.socialmediablog.platform.services.interaction.domain.aggregate.Bookmark;
import com.socialmediablog.platform.services.interaction.domain.repository.BookmarkRepository;
import com.socialmediablog.platform.services.interaction.domain.vo.ArticleId;
import com.socialmediablog.platform.services.interaction.domain.vo.BookmarkId;
import com.socialmediablog.platform.services.interaction.domain.vo.InteractorId;
import com.socialmediablog.platform.services.interaction.infrastructure.entity.JpaBookmarkEntity;
import com.socialmediablog.platform.services.interaction.infrastructure.persistence.SpringDataJpaBookmarkRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaBookmarkRepositoryAdapter implements BookmarkRepository {

    private final SpringDataJpaBookmarkRepository repository;

    public JpaBookmarkRepositoryAdapter(SpringDataJpaBookmarkRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Bookmark> findById(BookmarkId id) {
        return repository.findById(id.value()).map(JpaBookmarkEntity::toDomain);
    }

    @Override
    public Optional<Bookmark> findByUserIdAndArticleId(InteractorId userId, ArticleId articleId) {
        return repository.findByUserIdAndArticleId(userId.value(), articleId.value()).map(JpaBookmarkEntity::toDomain);
    }

    @Override
    public List<Bookmark> findByUserId(InteractorId userId) {
        return repository.findByUserId(userId.value()).stream()
                .map(JpaBookmarkEntity::toDomain)
                .toList();
    }

    @Override
    public Bookmark save(Bookmark bookmark) {
        return repository.save(JpaBookmarkEntity.fromDomain(bookmark)).toDomain();
    }
}
