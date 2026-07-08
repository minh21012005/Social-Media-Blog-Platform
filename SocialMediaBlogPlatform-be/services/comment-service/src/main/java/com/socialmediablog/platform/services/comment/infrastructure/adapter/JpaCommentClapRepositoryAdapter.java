package com.socialmediablog.platform.services.comment.infrastructure.adapter;

import com.socialmediablog.platform.services.comment.domain.aggregate.CommentClap;
import com.socialmediablog.platform.services.comment.domain.repository.CommentClapRepository;
import com.socialmediablog.platform.services.comment.domain.vo.CommentId;
import com.socialmediablog.platform.services.comment.infrastructure.entity.JpaCommentClapEntity;
import com.socialmediablog.platform.services.comment.infrastructure.persistence.SpringDataJpaCommentClapRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JpaCommentClapRepositoryAdapter implements CommentClapRepository {

    private final SpringDataJpaCommentClapRepository jpaRepository;

    public JpaCommentClapRepositoryAdapter(SpringDataJpaCommentClapRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public CommentClap save(CommentClap clap) {
        JpaCommentClapEntity entity = new JpaCommentClapEntity(
                clap.id(),
                clap.commentId().value(),
                clap.userId(),
                clap.createdAt()
        );
        jpaRepository.save(entity);
        return clap;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentClap> findByCommentIdAndUserId(CommentId commentId, UUID userId) {
        return jpaRepository.findByCommentIdAndUserId(commentId.value(), userId).stream()
                .map(entity -> CommentClap.restore(
                        entity.getId(),
                        entity.getCommentId(),
                        entity.getUserId(),
                        entity.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteByCommentIdAndUserId(CommentId commentId, UUID userId) {
        jpaRepository.deleteByCommentIdAndUserId(commentId.value(), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<CommentId> findClappedCommentIds(UUID userId, List<CommentId> commentIds) {
        if (commentIds.isEmpty()) {
            return Set.of();
        }
        
        List<UUID> ids = commentIds.stream().map(CommentId::value).collect(Collectors.toList());
        List<UUID> clappedIds = jpaRepository.findClappedCommentIds(userId, ids);
        
        return clappedIds.stream().map(CommentId::of).collect(Collectors.toSet());
    }
}
