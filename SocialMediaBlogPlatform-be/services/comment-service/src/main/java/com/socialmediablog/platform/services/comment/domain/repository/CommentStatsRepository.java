package com.socialmediablog.platform.services.comment.domain.repository;

import com.socialmediablog.platform.services.comment.domain.aggregate.CommentStats;
import com.socialmediablog.platform.services.comment.domain.vo.CommentId;
import java.util.Optional;

public interface CommentStatsRepository {

    Optional<CommentStats> findByCommentId(CommentId commentId);

    CommentStats save(CommentStats commentStats);
}
