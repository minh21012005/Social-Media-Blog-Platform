package com.socialmediablog.platform.services.comment.application.usecase;

import com.socialmediablog.platform.services.comment.application.command.CreateCommentCommand;
import com.socialmediablog.platform.services.comment.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.comment.application.port.in.CreateCommentUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.ListArticleCommentsUseCase;
import com.socialmediablog.platform.services.comment.application.port.out.CommentEventPublisher;
import com.socialmediablog.platform.services.comment.application.query.ListArticleCommentsQuery;
import com.socialmediablog.platform.services.comment.application.result.CommentStatsView;
import com.socialmediablog.platform.services.comment.application.result.CommentView;
import com.socialmediablog.platform.services.comment.application.result.ServiceStatus;
import com.socialmediablog.platform.services.comment.domain.aggregate.Comment;
import com.socialmediablog.platform.services.comment.domain.aggregate.CommentStats;
import com.socialmediablog.platform.services.comment.domain.event.CommentCreatedEvent;
import com.socialmediablog.platform.services.comment.domain.model.CommentStatus;
import com.socialmediablog.platform.services.comment.domain.repository.CommentRepository;
import com.socialmediablog.platform.services.comment.domain.repository.CommentStatsRepository;
import com.socialmediablog.platform.services.comment.domain.vo.ArticleId;
import com.socialmediablog.platform.services.comment.domain.vo.AuthorId;
import com.socialmediablog.platform.services.comment.domain.vo.CommentContent;
import java.util.Comparator;
import java.util.List;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentApplicationService implements GetServiceStatusUseCase, CreateCommentUseCase, ListArticleCommentsUseCase {

    private final CommentRepository commentRepository;
    private final CommentStatsRepository commentStatsRepository;
    private final CommentEventPublisher commentEventPublisher;
    private final Clock clock;

    public CommentApplicationService(
            CommentRepository commentRepository,
            CommentStatsRepository commentStatsRepository,
            CommentEventPublisher commentEventPublisher,
            Clock clock
    ) {
        this.commentRepository = commentRepository;
        this.commentStatsRepository = commentStatsRepository;
        this.commentEventPublisher = commentEventPublisher;
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
    @Transactional(readOnly = true)
    public List<CommentView> execute(ListArticleCommentsQuery query) {
        return commentRepository.findByArticleId(ArticleId.of(query.articleId())).stream()
                .filter(comment -> comment.parentCommentId() == null)
                .filter(comment -> comment.status() == CommentStatus.ACTIVE || comment.status() == CommentStatus.EDITED)
                .sorted(Comparator.comparing(Comment::createdAt).reversed())
                .map(comment -> CommentView.from(
                        comment,
                        commentStatsRepository.findByCommentId(comment.id())
                                .map(CommentStatsView::from)
                                .orElseGet(CommentStatsView::empty)
                ))
                .toList();
    }
}
