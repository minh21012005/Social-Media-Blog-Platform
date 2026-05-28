package com.socialmediablog.platform.services.comment.infrastructure.adapter;

import com.socialmediablog.platform.services.comment.domain.aggregate.Comment;
import com.socialmediablog.platform.services.comment.domain.repository.CommentRepository;
import com.socialmediablog.platform.services.comment.domain.vo.ArticleId;
import com.socialmediablog.platform.services.comment.domain.vo.CommentId;
import com.socialmediablog.platform.services.comment.infrastructure.entity.JpaCommentEntity;
import com.socialmediablog.platform.services.comment.infrastructure.persistence.SpringDataJpaCommentRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaCommentRepositoryAdapter implements CommentRepository {

    private final SpringDataJpaCommentRepository repository;

    public JpaCommentRepositoryAdapter(SpringDataJpaCommentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Comment> findById(CommentId id) {
        return repository.findById(id.value()).map(JpaCommentEntity::toDomain);
    }

    @Override
    public List<Comment> findByArticleId(ArticleId articleId) {
        return repository.findByArticleId(articleId.value()).stream()
                .map(JpaCommentEntity::toDomain)
                .toList();
    }

    @Override
    public Comment save(Comment comment) {
        return repository.save(JpaCommentEntity.fromDomain(comment)).toDomain();
    }
}
