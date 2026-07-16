package com.socialmediablog.platform.services.comment.infrastructure.adapter;

import com.socialmediablog.platform.services.comment.domain.aggregate.Comment;
import com.socialmediablog.platform.services.comment.domain.vo.ArticleId;
import com.socialmediablog.platform.services.comment.domain.vo.CommentId;
import com.socialmediablog.platform.services.comment.domain.model.CommentStatus;
import com.socialmediablog.platform.services.comment.domain.repository.CommentRepository;
import com.socialmediablog.platform.services.comment.infrastructure.entity.JpaCommentEntity;
import com.socialmediablog.platform.services.comment.infrastructure.persistence.SpringDataJpaCommentRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
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
    public Optional<Comment> findByIdForUpdate(CommentId id) {
        return repository.findByIdForUpdate(id.value()).map(JpaCommentEntity::toDomain);
    }

    @Override
    public long countByParentCommentId(CommentId parentCommentId) {
        return repository.countByParentCommentIdAndStatusIn(
                parentCommentId.value(),
                List.of(CommentStatus.ACTIVE.name(), CommentStatus.EDITED.name()));
    }

    @Override
    public long countVisibleByArticleId(ArticleId articleId) {
        return repository.countByArticleIdAndStatusIn(
                articleId.value(),
                List.of(CommentStatus.ACTIVE.name(), CommentStatus.EDITED.name()));
    }

    @Override
    public List<Comment> findRootCommentsByArticleId(ArticleId articleId, int page, int size, String sortBy) {
        if ("MOST_CLAPS".equals(sortBy)) {
            return repository.findRootCommentsByArticleIdOrderByMostClaps(articleId.value(), PageRequest.of(page, size)).stream()
                    .map(JpaCommentEntity::toDomain)
                    .toList();
        }
        if ("MOST_REPLIES".equals(sortBy)) {
            return repository.findRootCommentsByArticleIdOrderByMostReplies(articleId.value(), PageRequest.of(page, size)).stream()
                    .map(JpaCommentEntity::toDomain)
                    .toList();
        }

        Sort baseSort = switch (sortBy) {
            case "OLDEST" -> Sort.by(Sort.Direction.ASC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
        Sort sort = Sort.by(Sort.Order.desc("pinnedAt").nullsLast()).and(baseSort);
        return repository.findRootCommentsByArticleId(articleId.value(), PageRequest.of(page, size, sort)).stream()
                .map(JpaCommentEntity::toDomain)
                .toList();
    }

    @Override
    public long countRootCommentsByArticleId(ArticleId articleId) {
        return repository.countRootCommentsByArticleId(articleId.value());
    }

    @Override
    public Optional<Comment> findPinnedByArticleId(ArticleId articleId) {
        return repository.findByArticleIdAndPinnedAtIsNotNull(articleId.value()).map(JpaCommentEntity::toDomain);
    }

    @Override
    public List<Comment> findRepliesByParentCommentId(CommentId parentCommentId, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");
        return repository
                .findByParentCommentIdOrderByCreatedAtAsc(parentCommentId.value(), PageRequest.of(page, size, sort))
                .stream()
                .map(JpaCommentEntity::toDomain)
                .toList();
    }

    @Override
    public long countVisibleRepliesByParentCommentId(CommentId parentCommentId) {
        return countByParentCommentId(parentCommentId);
    }

    @Override
    public Comment save(Comment comment) {
        return repository.save(JpaCommentEntity.fromDomain(comment)).toDomain();
    }
}
