package com.socialmediablog.platform.services.comment.domain.repository;

import com.socialmediablog.platform.services.comment.domain.aggregate.Comment;
import com.socialmediablog.platform.services.comment.domain.vo.CommentId;
import java.util.Optional;

public interface CommentRepository {

    Optional<Comment> findById(CommentId id);

    Comment save(Comment comment);
}
