package com.socialmediablog.platform.services.comment.domain.repository;

import com.socialmediablog.platform.services.comment.domain.aggregate.Comment;
import com.socialmediablog.platform.services.comment.domain.vo.ArticleId;
import com.socialmediablog.platform.services.comment.domain.vo.CommentId;
import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    Optional<Comment> findById(CommentId id);

    List<Comment> findByArticleId(ArticleId articleId);

    Comment save(Comment comment);
}
