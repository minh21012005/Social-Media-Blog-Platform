package com.socialmediablog.platform.services.comment.application.usecase;

import com.socialmediablog.platform.services.comment.application.command.CreateCommentCommand;
import com.socialmediablog.platform.services.comment.application.command.ClapCommentCommand;
import com.socialmediablog.platform.services.comment.application.command.UndoClapCommentCommand;
import com.socialmediablog.platform.services.comment.application.port.in.ClapCommentUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.UndoClapCommentUseCase;
import com.socialmediablog.platform.services.comment.domain.aggregate.CommentClap;
import com.socialmediablog.platform.services.comment.domain.repository.CommentClapRepository;
import com.socialmediablog.platform.services.comment.domain.event.CommentClappedEvent;
import java.util.Set;
import java.util.stream.Collectors;
import com.socialmediablog.platform.services.comment.application.command.DeleteCommentCommand;
import com.socialmediablog.platform.services.comment.application.command.EditCommentCommand;
import com.socialmediablog.platform.services.comment.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.comment.application.command.ReplyCommentCommand;
import com.socialmediablog.platform.services.comment.application.exception.CommentAlreadyDeletedException;
import com.socialmediablog.platform.services.comment.application.exception.CommentNotFoundException;
import com.socialmediablog.platform.services.comment.application.exception.CommentPermissionDeniedException;
import com.socialmediablog.platform.services.comment.application.port.in.CountArticleCommentsUseCase;
import com.socialmediablog.platform.services.comment.application.query.CountArticleCommentsQuery;
import com.socialmediablog.platform.services.comment.application.port.in.CreateCommentUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.DeleteCommentUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.EditCommentUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.ListArticleCommentsUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.ListCommentRepliesUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.ReplyCommentUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.PinCommentUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.UnpinCommentUseCase;
import com.socialmediablog.platform.services.comment.application.command.PinCommentCommand;
import com.socialmediablog.platform.services.comment.application.command.UnpinCommentCommand;
import com.socialmediablog.platform.services.comment.application.port.out.ArticleCommentPolicyPort;
import com.socialmediablog.platform.services.comment.application.port.out.CommentEventPublisher;
import com.socialmediablog.platform.services.comment.application.query.ListArticleCommentsQuery;
import com.socialmediablog.platform.services.comment.application.query.ListCommentRepliesQuery;
import com.socialmediablog.platform.services.comment.application.result.CommentStatsView;
import com.socialmediablog.platform.services.comment.application.result.CommentView;
import com.socialmediablog.platform.services.comment.application.result.PageResult;
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
public class CommentApplicationService
                implements GetServiceStatusUseCase, CreateCommentUseCase, EditCommentUseCase, DeleteCommentUseCase,
                ListArticleCommentsUseCase, ListCommentRepliesUseCase, ReplyCommentUseCase, CountArticleCommentsUseCase,
                PinCommentUseCase, UnpinCommentUseCase, ClapCommentUseCase, UndoClapCommentUseCase {

        private final CommentRepository commentRepository;
        private final CommentStatsRepository commentStatsRepository;
        private final CommentClapRepository commentClapRepository;
        private final CommentEventPublisher commentEventPublisher;
        private final ArticleCommentPolicyPort articleCommentPolicyPort;
        private final Clock clock;

        public CommentApplicationService(
                        CommentRepository commentRepository,
                        CommentStatsRepository commentStatsRepository,
                        CommentClapRepository commentClapRepository,
                        CommentEventPublisher commentEventPublisher,
                        ArticleCommentPolicyPort articleCommentPolicyPort,
                        Clock clock) {
                this.commentRepository = commentRepository;
                this.commentStatsRepository = commentStatsRepository;
                this.commentClapRepository = commentClapRepository;
                this.commentEventPublisher = commentEventPublisher;
                this.articleCommentPolicyPort = articleCommentPolicyPort;
                this.clock = clock;
        }

        @Override
        @Transactional
        public void execute(ClapCommentCommand command) {
                Instant now = clock.instant();
                CommentId commentId = CommentId.of(command.commentId());
                Comment comment = commentRepository.findById(commentId)
                                .orElseThrow(() -> new CommentNotFoundException(command.commentId()));
                if (comment.isDeleted()) {
                        throw new IllegalArgumentException("Cannot clap a deleted comment");
                }

                CommentClap clap = CommentClap.create(commentId, command.requesterId(), now);
                commentClapRepository.save(clap);

                CommentStats stats = commentStatsRepository.findByCommentId(commentId)
                                .orElseGet(() -> CommentStats.empty(commentId, now));
                commentStatsRepository.save(stats.incrementClapCount(now));

                commentEventPublisher.publish(comment.id().value(), CommentClappedEvent.create(
                                comment.id().value(),
                                command.requesterId(),
                                now));
        }

        @Override
        @Transactional
        public long execute(UndoClapCommentCommand command) {
                Instant now = clock.instant();
                CommentId commentId = CommentId.of(command.commentId());
                Comment comment = commentRepository.findById(commentId)
                                .orElseThrow(() -> new CommentNotFoundException(command.commentId()));

                var claps = commentClapRepository.findByCommentIdAndUserId(commentId, command.requesterId());
                if (claps.isEmpty()) {
                        return 0;
                }
                long clapsToRemove = claps.size();

                commentClapRepository.deleteByCommentIdAndUserId(commentId, command.requesterId());

                commentStatsRepository.findByCommentId(commentId)
                                .map(stats -> stats.decrementClapCount(clapsToRemove, now))
                                .ifPresent(commentStatsRepository::save);
                
                return clapsToRemove;
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
                                now);
                Comment savedComment = commentRepository.save(comment);
                CommentStats stats = commentStatsRepository.save(CommentStats.empty(savedComment.id(), now));
                commentEventPublisher.publish(savedComment.id().value(), new CommentCreatedEvent(
                                UUID.randomUUID(),
                                savedComment.id().value(),
                                savedComment.articleId().value(),
                                savedComment.authorId().value(),
                                now));
                return CommentView.from(savedComment, CommentStatsView.from(stats), false);
        }

        @Override
        @Transactional
        public CommentView execute(ReplyCommentCommand command) {
                Instant now = clock.instant();
                Comment parentComment = commentRepository.findById(CommentId.of(command.parentCommentId()))
                                .orElseThrow(() -> new CommentNotFoundException(command.parentCommentId()));
                if (parentComment.isDeleted()) {
                        throw new IllegalArgumentException("Deleted comment cannot be replied to");
                }
                if (parentComment.isReply()) {
                        throw new IllegalArgumentException("Reply-to-reply is not supported");
                }

                Comment reply = Comment.create(
                                parentComment.articleId(),
                                AuthorId.of(command.authorId()),
                                parentComment.id(),
                                CommentContent.of(command.content()),
                                now);
                Comment savedReply = commentRepository.save(reply);
                CommentStats replyStats = commentStatsRepository.save(CommentStats.empty(savedReply.id(), now));
                CommentStats parentStats = commentStatsRepository.findByCommentId(parentComment.id())
                                .orElseGet(() -> CommentStats.empty(parentComment.id(), now));
                commentStatsRepository.save(parentStats.incrementReplyCount(now));
                commentEventPublisher.publish(savedReply.id().value(), new CommentCreatedEvent(
                                UUID.randomUUID(),
                                savedReply.id().value(),
                                savedReply.articleId().value(),
                                savedReply.authorId().value(),
                                now));
                return CommentView.from(savedReply, CommentStatsView.from(replyStats), false);
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
                                now));
                commentEventPublisher.publish(savedComment.id().value(), new CommentEditedEvent(
                                UUID.randomUUID(),
                                savedComment.id().value(),
                                savedComment.articleId().value(),
                                savedComment.authorId().value(),
                                now));
                CommentStatsView stats = commentStatsRepository.findByCommentId(savedComment.id())
                                .map(CommentStatsView::from)
                                .orElseGet(CommentStatsView::empty);
                return CommentView.from(savedComment, stats, false);
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
                if (comment.isReply()) {
                        commentStatsRepository.findByCommentId(comment.parentCommentId())
                                        .map(stats -> stats.decrementReplyCount(now))
                                        .ifPresent(commentStatsRepository::save);
                }
                commentEventPublisher.publish(savedComment.id().value(), new CommentDeletedEvent(
                                UUID.randomUUID(),
                                savedComment.id().value(),
                                savedComment.articleId().value(),
                                savedComment.authorId().value(),
                                now));
        }

        @Override
        @Transactional
        public CommentView execute(PinCommentCommand command) {
                Instant now = clock.instant();
                Comment comment = commentRepository.findById(CommentId.of(command.commentId()))
                                .orElseThrow(() -> new CommentNotFoundException(command.commentId()));

                // Verify requester is the article author
                boolean isArticleAuthor = articleCommentPolicyPort.findByArticleId(comment.articleId().value())
                                .map(policy -> command.requesterId().equals(policy.authorId()))
                                .orElse(false);

                if (!isArticleAuthor) {
                        throw new CommentPermissionDeniedException();
                }

                // Unpin existing pinned comment if any
                commentRepository.findPinnedByArticleId(comment.articleId())
                                .filter(existing -> !existing.id().equals(comment.id()))
                                .ifPresent(existing -> commentRepository.save(existing.unpin(now)));

                Comment savedComment = commentRepository.save(comment.pin(now));
                CommentStatsView stats = commentStatsRepository.findByCommentId(savedComment.id())
                                .map(CommentStatsView::from)
                                .orElseGet(CommentStatsView::empty);
                return CommentView.from(savedComment, stats, false);
        }

        @Override
        @Transactional
        public CommentView execute(UnpinCommentCommand command) {
                Instant now = clock.instant();
                Comment comment = commentRepository.findById(CommentId.of(command.commentId()))
                                .orElseThrow(() -> new CommentNotFoundException(command.commentId()));

                // Verify requester is the article author
                boolean isArticleAuthor = articleCommentPolicyPort.findByArticleId(comment.articleId().value())
                                .map(policy -> command.requesterId().equals(policy.authorId()))
                                .orElse(false);

                if (!isArticleAuthor) {
                        throw new CommentPermissionDeniedException();
                }

                Comment savedComment = commentRepository.save(comment.unpin(now));
                CommentStatsView stats = commentStatsRepository.findByCommentId(savedComment.id())
                                .map(CommentStatsView::from)
                                .orElseGet(CommentStatsView::empty);
                return CommentView.from(savedComment, stats, false);
        }

        @Override
        @Transactional(readOnly = true)
        public PageResult<CommentView> execute(ListArticleCommentsQuery query) {
                ArticleId articleId = ArticleId.of(query.articleId());
                List<Comment> comments = commentRepository.findRootCommentsByArticleId(
                                articleId, query.page(), query.size(), query.sortBy().name());

                Set<CommentId> clappedIds = query.currentUserId() != null
                                ? commentClapRepository.findClappedCommentIds(query.currentUserId(),
                                                comments.stream().map(Comment::id).collect(Collectors.toList()))
                                : Set.of();

                List<CommentView> items = comments.stream()
                                .map(c -> visibleRootCommentView(c, clappedIds.contains(c.id())))
                                .flatMap(Optional::stream)
                                .toList();
                long totalItems = commentRepository.countRootCommentsByArticleId(articleId);
                return PageResult.of(items, query.page(), query.size(), totalItems);
        }

        @Override
        @Transactional(readOnly = true)
        public PageResult<CommentView> execute(ListCommentRepliesQuery query) {
                CommentId parentCommentId = CommentId.of(query.commentId());
                commentRepository.findById(parentCommentId)
                                .orElseThrow(() -> new CommentNotFoundException(query.commentId()));
                List<Comment> replies = commentRepository.findRepliesByParentCommentId(
                                parentCommentId, query.page(), query.size());

                Set<CommentId> clappedIds = query.currentUserId() != null
                                ? commentClapRepository.findClappedCommentIds(query.currentUserId(),
                                                replies.stream().map(Comment::id).collect(Collectors.toList()))
                                : Set.of();

                List<CommentView> items = replies.stream()
                                .filter(this::isVisibleComment)
                                .map(comment -> CommentView.from(comment, statsWithComputedReplyCount(comment),
                                                clappedIds.contains(comment.id())))
                                .toList();
                long totalItems = commentRepository.countVisibleRepliesByParentCommentId(parentCommentId);
                return PageResult.of(items, query.page(), query.size(), totalItems);
        }

        private boolean canDelete(Comment comment, AuthorId requesterId) {
                if (comment.canBeDeletedBy(requesterId)) {
                        return true;
                }
                return articleCommentPolicyPort.findByArticleId(comment.articleId().value())
                                .map(policy -> requesterId.value().equals(policy.authorId()))
                                .orElse(false);
        }

        private Optional<CommentView> visibleRootCommentView(Comment comment, boolean clappedByCurrentUser) {
                CommentStatsView stats = statsWithComputedReplyCount(comment);
                if (isVisibleComment(comment)) {
                        return Optional.of(CommentView.from(comment, stats, clappedByCurrentUser));
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
                long replyCount = commentRepository.countByParentCommentId(comment.id());
                return new CommentStatsView(storedStats.clapCount(), replyCount, storedStats.lastInteractionAt());
        }

        @Override
        public long execute(CountArticleCommentsQuery query) {
                return commentRepository.countVisibleByArticleId(ArticleId.of(query.articleId()));
        }
}
