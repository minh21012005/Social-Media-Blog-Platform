package com.socialmediablog.platform.services.comment.infrastructure.adapter;

import com.socialmediablog.platform.services.comment.domain.aggregate.CommentStats;
import com.socialmediablog.platform.services.comment.domain.repository.CommentStatsRepository;
import com.socialmediablog.platform.services.comment.domain.vo.CommentId;
import com.socialmediablog.platform.services.comment.infrastructure.entity.JpaCommentStatsEntity;
import com.socialmediablog.platform.services.comment.infrastructure.persistence.SpringDataJpaCommentStatsRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaCommentStatsRepositoryAdapter implements CommentStatsRepository {

    private final SpringDataJpaCommentStatsRepository repository;

    public JpaCommentStatsRepositoryAdapter(SpringDataJpaCommentStatsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<CommentStats> findByCommentId(CommentId commentId) {
        return repository.findByCommentId(commentId.value()).map(JpaCommentStatsEntity::toDomain);
    }

    @Override
    public CommentStats save(CommentStats commentStats) {
        return repository.save(JpaCommentStatsEntity.fromDomain(commentStats)).toDomain();
    }
}
