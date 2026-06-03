package com.socialmediablog.platform.services.interaction.infrastructure.adapter;

import com.socialmediablog.platform.services.interaction.domain.aggregate.Like;
import com.socialmediablog.platform.services.interaction.domain.repository.LikeRepository;
import com.socialmediablog.platform.services.interaction.domain.vo.ArticleId;
import com.socialmediablog.platform.services.interaction.domain.vo.InteractorId;
import com.socialmediablog.platform.services.interaction.infrastructure.persistence.SpringDataJpaLikeRepository;
import com.socialmediablog.platform.services.interaction.infrastructure.persistence.entity.LikeEntity;
import com.socialmediablog.platform.services.interaction.infrastructure.persistence.entity.LikeEntityId;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaLikeRepositoryAdapter implements LikeRepository {

    private final SpringDataJpaLikeRepository repository;

    public JpaLikeRepositoryAdapter(SpringDataJpaLikeRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Like like) {
        repository.save(new LikeEntity(
                new LikeEntityId(like.getUserId().value(), like.getArticleId().value()),
                like.getCreatedAt()
        ));
    }

    @Override
    public void delete(Like like) {
        repository.delete(new LikeEntity(
                new LikeEntityId(like.getUserId().value(), like.getArticleId().value()),
                like.getCreatedAt()
        ));
    }

    @Override
    public Optional<Like> findById(InteractorId userId, ArticleId articleId) {
        return repository.findById(new LikeEntityId(userId.value(), articleId.value()))
                .map(likeEntity -> Like.create(
                        InteractorId.of(likeEntity.getId().getUserId()),
                        ArticleId.of(likeEntity.getId().getArticleId()),
                        likeEntity.getCreatedAt()
                ));
    }

    @Override
    public long countByArticle(ArticleId articleId) {
        return repository.countById_ArticleId(articleId.value());
    }
}
