package com.socialmediablog.platform.services.comment.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import com.socialmediablog.platform.services.comment.application.command.ClapCommentCommand;
import com.socialmediablog.platform.services.comment.application.command.UndoClapCommentCommand;
import com.socialmediablog.platform.services.comment.domain.aggregate.Comment;
import com.socialmediablog.platform.services.comment.domain.aggregate.CommentClap;
import com.socialmediablog.platform.services.comment.domain.aggregate.CommentStats;
import com.socialmediablog.platform.services.comment.domain.repository.CommentClapRepository;
import com.socialmediablog.platform.services.comment.domain.repository.CommentRepository;
import com.socialmediablog.platform.services.comment.domain.repository.CommentStatsRepository;
import com.socialmediablog.platform.services.comment.domain.vo.CommentId;
import com.socialmediablog.platform.services.comment.application.port.out.CommentEventPublisher;
import com.socialmediablog.platform.services.comment.application.port.out.ArticleCommentPolicyPort;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    void execute_clapCommentCommand_savesClapAndIncrementsStats() {
        UUID commentUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        CommentId commentId = CommentId.of(commentUuid);
        
        Comment mockComment = Mockito.mock(Comment.class);
        when(mockComment.isDeleted()).thenReturn(false);
        when(mockComment.id()).thenReturn(commentId);
        
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        when(commentStatsRepository.findByCommentId(commentId)).thenReturn(Optional.of(CommentStats.empty(commentId, clock.instant())));
        
        service.execute(new ClapCommentCommand(commentUuid, userUuid));
        
        verify(commentClapRepository).save(any(CommentClap.class));
        verify(commentStatsRepository).save(any(CommentStats.class));
        verify(commentEventPublisher).publish(any(), any());
    }

    @Test
    void execute_undoClapCommentCommand_deletesClapsAndDecrementsStats() {
        UUID commentUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        CommentId commentId = CommentId.of(commentUuid);
        
        Comment mockComment = Mockito.mock(Comment.class);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        
        CommentClap clap1 = CommentClap.create(commentId, userUuid, clock.instant());
        CommentClap clap2 = CommentClap.create(commentId, userUuid, clock.instant());
        
        when(commentClapRepository.findByCommentIdAndUserId(commentId, userUuid)).thenReturn(List.of(clap1, clap2));
        
        CommentStats stats = CommentStats.empty(commentId, clock.instant()).incrementClapCount(clock.instant()).incrementClapCount(clock.instant());
        when(commentStatsRepository.findByCommentId(commentId)).thenReturn(Optional.of(stats));
        
        service.execute(new UndoClapCommentCommand(commentUuid, userUuid));
        
        verify(commentClapRepository).deleteByCommentIdAndUserId(commentId, userUuid);
        verify(commentStatsRepository).save(any(CommentStats.class));
    }
}
