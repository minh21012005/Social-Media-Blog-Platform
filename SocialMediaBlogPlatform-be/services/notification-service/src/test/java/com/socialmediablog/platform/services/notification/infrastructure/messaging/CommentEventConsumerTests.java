package com.socialmediablog.platform.services.notification.infrastructure.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.notification.domain.aggregate.Notification;
import com.socialmediablog.platform.services.notification.domain.model.NotificationType;
import com.socialmediablog.platform.services.notification.domain.repository.NotificationRepository;
import com.socialmediablog.platform.services.notification.infrastructure.entity.JpaProcessedEventEntity;
import com.socialmediablog.platform.services.notification.infrastructure.feign.ArticleAuthorResponse;
import com.socialmediablog.platform.services.notification.infrastructure.feign.ArticleServiceFeignClient;
import com.socialmediablog.platform.services.notification.infrastructure.persistence.SpringDataJpaProcessedEventRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentEventConsumerTests {

    @Mock private NotificationRepository notificationRepository;
    @Mock private SpringDataJpaProcessedEventRepository processedEventRepository;
    @Mock private ArticleServiceFeignClient articleServiceFeignClient;

    private CommentEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new CommentEventConsumer(
                notificationRepository,
                new ObjectMapper(),
                processedEventRepository,
                articleServiceFeignClient
        );
    }

    @Test
    void createsReplyNotificationForParentCommentAuthor() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID replyId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        UUID replyAuthorId = UUID.randomUUID();
        UUID parentAuthorId = UUID.randomUUID();
        when(articleServiceFeignClient.getArticleAuthor(articleId))
                .thenReturn(ApiResponse.success(new ArticleAuthorResponse(parentAuthorId)));

        consumer.consume("""
                {"eventId":"%s","replyId":"%s","parentCommentId":"%s","articleId":"%s",\
                "authorId":"%s","parentAuthorId":"%s","eventType":"comment.replied"}
                """.formatted(eventId, replyId, parentId, articleId, replyAuthorId, parentAuthorId));

        ArgumentCaptor<Notification> notification = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notification.capture());
        assertThat(notification.getValue().type()).isEqualTo(NotificationType.COMMENT_REPLIED);
        assertThat(notification.getValue().recipientId().value()).isEqualTo(parentAuthorId);
        assertThat(notification.getValue().subjectId()).isEqualTo(replyId);
        verify(processedEventRepository).save(any(JpaProcessedEventEntity.class));
    }

    @Test
    void createsOnlyOneCommentClapNotificationPerActorAndComment() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        UUID commentAuthorId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        when(notificationRepository.existsByRecipientAndActorAndTypeAndTarget(
                any(), any(), any(), any())).thenReturn(false);

        consumer.consume("""
                {"eventId":"%s","commentId":"%s","articleId":"%s","commentAuthorId":"%s",\
                "userId":"%s","parentCommentId":null,"eventType":"comment.clapped"}
                """.formatted(eventId, commentId, articleId, commentAuthorId, actorId));

        ArgumentCaptor<Notification> notification = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notification.capture());
        assertThat(notification.getValue().type()).isEqualTo(NotificationType.COMMENT_CLAPPED);
        assertThat(notification.getValue().subjectId()).isEqualTo(commentId);
    }

    @Test
    void doesNotMarkEventProcessedWhenDependencyFails() {
        UUID eventId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        when(articleServiceFeignClient.getArticleAuthor(articleId)).thenThrow(new IllegalStateException("offline"));

        String message = """
                {"eventId":"%s","commentId":"%s","articleId":"%s","authorId":"%s",\
                "eventType":"comment.created"}
                """.formatted(eventId, UUID.randomUUID(), articleId, UUID.randomUUID());

        assertThatThrownBy(() -> consumer.consume(message)).isInstanceOf(IllegalStateException.class);
        verify(processedEventRepository, never()).save(any());
    }
}