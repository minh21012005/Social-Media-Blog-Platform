package com.socialmediablog.platform.services.comment.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.socialmediablog.platform.services.comment.application.command.ClapCommentCommand;
import com.socialmediablog.platform.services.comment.application.command.UndoClapCommentCommand;
import com.socialmediablog.platform.services.comment.application.port.out.ArticleCommentPolicyPort;
import com.socialmediablog.platform.services.comment.application.port.out.CommentEventPublisher;
import com.socialmediablog.platform.services.comment.domain.aggregate.Comment;
import com.socialmediablog.platform.services.comment.domain.aggregate.CommentClap;
import com.socialmediablog.platform.services.comment.domain.aggregate.CommentStats;
import com.socialmediablog.platform.services.comment.domain.repository.CommentClapRepository;
import com.socialmediablog.platform.services.comment.domain.repository.CommentRepository;
import com.socialmediablog.platform.services.comment.domain.repository.CommentStatsRepository;
import com.socialmediablog.platform.services.comment.domain.vo.ArticleId;
import com.socialmediablog.platform.services.comment.domain.vo.AuthorId;
import com.socialmediablog.platform.services.comment.domain.vo.CommentId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentApplicationServiceClapTests {

    @Mock private CommentRepository commentRepository;
    @Mock private CommentStatsRepository commentStatsRepository;
    @Mock private CommentClapRepository commentClapRepository;
    @Mock private CommentEventPublisher commentEventPublisher;
    @Mock private ArticleCommentPolicyPort articleCommentPolicyPort;

    private Clock clock;
    private CommentApplicationService service;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-05-28T00:00:00Z"), ZoneId.of("UTC"));
        service = new CommentApplicationService(
                commentRepository,
                commentStatsRepository,
                commentClapRepository,
                commentEventPublisher,
                articleCommentPolicyPort,
                clock
        );
    }

    @Test
    void clapComment_createsFirstClapAndIncrementsTotal() {
        UUID commentUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        CommentId commentId = CommentId.of(commentUuid);
        Comment comment = activeComment(commentId);

        when(commentRepository.findByIdForUpdate(commentId)).thenReturn(Optional.of(comment));
        when(commentClapRepository.findByCommentIdAndUserId(commentId, userUuid)).thenReturn(Optional.empty());
        when(commentStatsRepository.findByCommentId(commentId))
                .thenReturn(Optional.of(CommentStats.empty(commentId, clock.instant())));
        when(commentStatsRepository.save(any(CommentStats.class))).thenAnswer(invocation -> invocation.getArgument(0));

        long total = service.execute(new ClapCommentCommand(commentUuid, userUuid));

        ArgumentCaptor<CommentClap> clapCaptor = ArgumentCaptor.forClass(CommentClap.class);
        verify(commentClapRepository).save(clapCaptor.capture());
        assertThat(clapCaptor.getValue().clapCount()).isEqualTo(1);
        assertThat(total).isEqualTo(1);
        verify(commentEventPublisher).publish(any(), any());
    }

    @Test
    void clapComment_incrementsExistingUserClapCount() {
        UUID commentUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        CommentId commentId = CommentId.of(commentUuid);
        Comment comment = activeComment(commentId);
        CommentClap existing = CommentClap.create(commentId, userUuid, clock.instant());

        when(commentRepository.findByIdForUpdate(commentId)).thenReturn(Optional.of(comment));
        when(commentClapRepository.findByCommentIdAndUserId(commentId, userUuid))
                .thenReturn(Optional.of(existing));
        CommentStats stats = CommentStats.empty(commentId, clock.instant())
                .incrementClapCount(clock.instant());
        when(commentStatsRepository.findByCommentId(commentId)).thenReturn(Optional.of(stats));
        when(commentStatsRepository.save(any(CommentStats.class))).thenAnswer(invocation -> invocation.getArgument(0));

        long total = service.execute(new ClapCommentCommand(commentUuid, userUuid));

        ArgumentCaptor<CommentClap> clapCaptor = ArgumentCaptor.forClass(CommentClap.class);
        verify(commentClapRepository).save(clapCaptor.capture());
        assertThat(clapCaptor.getValue().clapCount()).isEqualTo(2);
        assertThat(total).isEqualTo(2);
    }

    @Test
    void undoCommentClap_removesAllClapsFromCurrentUser() {
        UUID commentUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        CommentId commentId = CommentId.of(commentUuid);
        Comment comment = Mockito.mock(Comment.class);
        CommentClap clap = CommentClap.restore(
                UUID.randomUUID(),
                commentUuid,
                userUuid,
                3,
                clock.instant(),
                clock.instant()
        );

        when(commentRepository.findByIdForUpdate(commentId)).thenReturn(Optional.of(comment));
        when(commentClapRepository.findByCommentIdAndUserId(commentId, userUuid))
                .thenReturn(Optional.of(clap));
        CommentStats stats = CommentStats.empty(commentId, clock.instant())
                .incrementClapCount(clock.instant())
                .incrementClapCount(clock.instant())
                .incrementClapCount(clock.instant());
        when(commentStatsRepository.findByCommentId(commentId)).thenReturn(Optional.of(stats));

        long removed = service.execute(new UndoClapCommentCommand(commentUuid, userUuid));

        assertThat(removed).isEqualTo(3);
        verify(commentClapRepository).deleteByCommentIdAndUserId(commentId, userUuid);
        verify(commentStatsRepository).save(any(CommentStats.class));
    }

    private Comment activeComment(CommentId commentId) {
        Comment comment = Mockito.mock(Comment.class);
        when(comment.isDeleted()).thenReturn(false);
        when(comment.id()).thenReturn(commentId);
        when(comment.articleId()).thenReturn(ArticleId.of(UUID.randomUUID()));
        when(comment.authorId()).thenReturn(AuthorId.of(UUID.randomUUID()));
        return comment;
    }
}
