package com.socialmediablog.platform.services.comment.application.usecase;

import com.socialmediablog.platform.services.comment.application.command.CreateCommentCommand;
import com.socialmediablog.platform.services.comment.application.command.DeleteCommentCommand;
import com.socialmediablog.platform.services.comment.application.command.EditCommentCommand;
import com.socialmediablog.platform.services.comment.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.comment.application.exception.CommentAlreadyDeletedException;
import com.socialmediablog.platform.services.comment.application.exception.CommentNotFoundException;
import com.socialmediablog.platform.services.comment.application.exception.CommentPermissionDeniedException;
import com.socialmediablog.platform.services.comment.application.port.in.CreateCommentUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.DeleteCommentUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.EditCommentUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.ListArticleCommentsUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.ListCommentRepliesUseCase;
import com.socialmediablog.platform.services.comment.application.port.out.ArticleCommentPolicyPort;
import com.socialmediablog.platform.services.comment.application.port.out.CommentEventPublisher;
import com.socialmediablog.platform.services.comment.application.query.ListArticleCommentsQuery;
import com.socialmediablog.platform.services.comment.application.query.ListCommentRepliesQuery;
import com.socialmediablog.platform.services.comment.application.result.CommentStatsView;
import com.socialmediablog.platform.services.comment.application.result.CommentView;
import com.socialmediablog.platform.services.comment.application.result.ServiceStatus;
import com.socialmediablog.platform.services.comment.domain.aggregate.Comment;
import com.socialmediablog.platform.services.comment.domain.aggregate.CommentStats;
import com.socialmediablog.platform.services.comment.domain.event.CommentCreatedEvent;
import com.socialmediablog.platform.services.comment.domain.event.CommentDeletedEvent;
import com.socialmediablog.platform.services.comment.domain.event.CommentEditedEvent;
import com.socialmediablog.platform.services.comment.domain.model.CommentStatus;
import com.socialmediablog.platform.services.comment.domain.repository.CommentRepository;
import com.socialmediablog.platform.services.comment.domain.repository.CommentStatsRepository;
import com.socialmediablog.platform.services.comment.domain.vo.ArticleId;
import com.socialmediablog.platform.services.comment.domain.vo.AuthorId;
import com.socialmediablog.platform.services.comment.domain.vo.CommentContent;
import com.socialmediablog.platform.services.comment.domain.vo.CommentId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentApplicationService implements GetServiceStatusUseCase, CreateCommentUseCase, EditCommentUseCase, DeleteCommentUseCase, ListArticleCommentsUseCase, ListCommentRepliesUseCase {

    private final CommentRepository commentRepository;
    private final CommentStatsRepository commentStatsRepository;
    private final CommentEventPublisher commentEventPublisher;
    private final ArticleCommentPolicyPort articleCommentPolicyPort;
    private final Clock clock;

    public CommentApplicationService(
            CommentRepository commentRepository,
            CommentStatsRepository commentStatsRepository,
            CommentEventPublisher commentEventPublisher,
            ArticleCommentPolicyPort articleCommentPolicyPort,
            Clock clock
    ) {
        this.commentRepository = commentRepository;
        this.commentStatsRepository = commentStatsRepository;
        this.commentEventPublisher = commentEventPublisher;
        this.articleCommentPolicyPort = articleCommentPolicyPort;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceStatus execute(GetServiceStatusCommand command) {
        return new ServiceStatus("comment-service", "comments", command.currentUserId());
    }

    @Override
    @Transactional
    public CommentView execute(CreateCommentCommand command) {
        Instant now = clock.instant();
        Comment comment = Comment.create(
                ArticleId.of(command.articleId()),
                AuthorId.of(command.authorId()),
                null,
                CommentContent.of(command.content()),
                now
        );
        Comment savedComment = commentRepository.save(comment);
        CommentStats stats = commentStatsRepository.save(CommentStats.empty(savedComment.id(), now));
        commentEventPublisher.publish(savedComment.id().value(), new CommentCreatedEvent(
                UUID.randomUUID(),
                savedComment.id().value(),
                savedComment.articleId().value(),
                savedComment.authorId().value(),
                now
        ));
        return CommentView.from(savedComment, CommentStatsView.from(stats));
    }

    @Override
    @Transactional
    public CommentView execute(EditCommentCommand command) {
        Instant now = clock.instant();
        Comment comment = commentRepository.findById(CommentId.of(command.commentId()))
                .orElseThrow(() -> new CommentNotFoundException(command.commentId()));
        AuthorId requesterId = AuthorId.of(command.requesterId());
        if (!comment.canBeEditedBy(requesterId)) {
            throw new CommentPermissionDeniedException();
        }
        if (comment.isDeleted()) {
            throw new CommentAlreadyDeletedException();
        }
        Comment savedComment = commentRepository.save(comment.edit(
                requesterId,
                CommentContent.of(command.content()),
                now
        ));
        commentEventPublisher.publish(savedComment.id().value(), new CommentEditedEvent(
                UUID.randomUUID(),
                savedComment.id().value(),
                savedComment.articleId().value(),
                savedComment.authorId().value(),
                now
        ));
        CommentStatsView stats = commentStatsRepository.findByCommentId(savedComment.id())
                .map(CommentStatsView::from)
                .orElseGet(CommentStatsView::empty);
        return CommentView.from(savedComment, stats);
    }

    @Override
    @Transactional
    public void execute(DeleteCommentCommand command) {
        Instant now = clock.instant();
        Comment comment = commentRepository.findById(CommentId.of(command.commentId()))
                .orElseThrow(() -> new CommentNotFoundException(command.commentId()));
        AuthorId requesterId = AuthorId.of(command.requesterId());
        if (!canDelete(comment, requesterId)) {
            throw new CommentPermissionDeniedException();
        }
        if (comment.isDeleted()) {
            throw new CommentAlreadyDeletedException();
        }
        Comment savedComment = commentRepository.save(comment.delete(requesterId, now));
        commentEventPublisher.publish(savedComment.id().value(), new CommentDeletedEvent(
                UUID.randomUUID(),
                savedComment.id().value(),
                savedComment.articleId().value(),
                savedComment.authorId().value(),
                now
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentView> execute(ListArticleCommentsQuery query) {
        return commentRepository.findByArticleId(ArticleId.of(query.articleId())).stream()
                .filter(comment -> comment.parentCommentId() == null)
                .sorted(Comparator.comparing(Comment::createdAt).reversed())
                .map(this::visibleRootCommentView)
                .flatMap(Optional::stream)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentView> execute(ListCommentRepliesQuery query) {
        CommentId parentCommentId = CommentId.of(query.commentId());
        commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new CommentNotFoundException(query.commentId()));
        return commentRepository.findByParentCommentId(parentCommentId).stream()
                .filter(this::isVisibleComment)
                .map(comment -> CommentView.from(comment, statsWithComputedReplyCount(comment)))
                .toList();
    }

    private boolean canDelete(Comment comment, AuthorId requesterId) {
        if (comment.canBeDeletedBy(requesterId)) {
            return true;
        }
        return articleCommentPolicyPort.findByArticleId(comment.articleId().value())
                .map(policy -> requesterId.value().equals(policy.authorId()))
                .orElse(false);
    }

    private Optional<CommentView> visibleRootCommentView(Comment comment) {
        CommentStatsView stats = statsWithComputedReplyCount(comment);
        if (isVisibleComment(comment)) {
            return Optional.of(CommentView.from(comment, stats));
        }
        if (comment.status() == CommentStatus.DELETED && stats.replyCount() > 0) {
            return Optional.of(CommentView.deletedPlaceholder(comment, stats));
        }
        return Optional.empty();
    }

    private boolean isVisibleComment(Comment comment) {
        return comment.status() == CommentStatus.ACTIVE || comment.status() == CommentStatus.EDITED;
    }

    private CommentStatsView statsWithComputedReplyCount(Comment comment) {
        CommentStatsView storedStats = commentStatsRepository.findByCommentId(comment.id())
                .map(CommentStatsView::from)
                .orElseGet(CommentStatsView::empty);
        long replyCount = Math.max(storedStats.replyCount(), commentRepository.countByParentCommentId(comment.id()));
        return new CommentStatsView(storedStats.clapCount(), replyCount, storedStats.lastInteractionAt());
    }
}
