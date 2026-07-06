package com.socialmediablog.platform.services.comment.domain.repository;

import com.socialmediablog.platform.services.comment.domain.aggregate.Comment;
import com.socialmediablog.platform.services.comment.domain.vo.ArticleId;
import com.socialmediablog.platform.services.comment.domain.vo.CommentId;
import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    Optional<Comment> findById(CommentId id);

    long countByParentCommentId(CommentId parentCommentId);

    long countVisibleByArticleId(ArticleId articleId);

    List<Comment> findRootCommentsByArticleId(ArticleId articleId, int page, int size, String sortBy);

    long countRootCommentsByArticleId(ArticleId articleId);

    List<Comment> findRepliesByParentCommentId(CommentId parentCommentId, int page, int size);

    long countVisibleRepliesByParentCommentId(CommentId parentCommentId);

    Comment save(Comment comment);
}
