package com.socialmediablog.platform.services.comment.domain.repository;

import com.socialmediablog.platform.services.comment.domain.aggregate.CommentClap;
import com.socialmediablog.platform.services.comment.domain.vo.CommentId;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface CommentClapRepository {
    CommentClap save(CommentClap clap);
    List<CommentClap> findByCommentIdAndUserId(CommentId commentId, UUID userId);
    void deleteByCommentIdAndUserId(CommentId commentId, UUID userId);
    Set<CommentId> findClappedCommentIds(UUID userId, List<CommentId> commentIds);
}
