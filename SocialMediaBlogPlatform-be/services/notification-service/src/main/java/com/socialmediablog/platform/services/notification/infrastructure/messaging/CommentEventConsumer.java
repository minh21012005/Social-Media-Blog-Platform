package com.socialmediablog.platform.services.notification.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialmediablog.platform.services.notification.domain.aggregate.Notification;
import com.socialmediablog.platform.services.notification.domain.model.NotificationType;
import com.socialmediablog.platform.services.notification.domain.repository.NotificationRepository;
import com.socialmediablog.platform.services.notification.domain.vo.RecipientId;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens for comment.events Kafka topic.
 * Event payload (serialized from CommentCreatedEvent):
 *   {"eventId":"...", "commentId":"...", "articleId":"...", "authorId":"...", "occurredAt":"...", "eventType":"comment.created"}
 *
 * NOTE: CommentCreatedEvent does not contain the article author's ID.
 * To notify the article author, we would need to call article-service to resolve authorId from articleId.
 * For simplicity, the current implementation records the notification with articleId as subjectId
 * and uses authorId as the recipient (comment author's ID, not article author's).
 * TODO: Call article-service to get the article author and send notification to them.
 */
@Component
public class CommentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(CommentEventConsumer.class);

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    public CommentEventConsumer(
            NotificationRepository notificationRepository,
            ObjectMapper objectMapper
    ) {
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "comment.events", groupId = "notification-service")
    public void consume(String message) {
        try {
            JsonNode payload = objectMapper.readTree(message);
            String eventType = payload.path("eventType").asText();

            if (!"comment.created".equals(eventType)) {
                return;
            }

            UUID commentId = UUID.fromString(payload.path("commentId").asText());
            UUID articleId = UUID.fromString(payload.path("articleId").asText());
            UUID commentAuthorId = UUID.fromString(payload.path("authorId").asText());
            Instant now = Instant.now();

            // TODO: Call article-service via Feign to get article.authorId
            // Then create COMMENT_CREATED notification for the article author
            // For now: create a self-notification for the comment author (placeholder)
            Notification notification = Notification.create(
                    RecipientId.of(commentAuthorId),
                    commentAuthorId,
                    NotificationType.COMMENT_CREATED,
                    "Article",
                    articleId,
                    "Bình luận của bạn đã được đăng",
                    null,
                    now
            );
            notificationRepository.save(notification);
            log.info("[NotificationConsumer] Saved COMMENT_CREATED notification: commentId={} articleId={}", commentId, articleId);

        } catch (Exception e) {
            log.error("[NotificationConsumer] Failed to process comment event: {}", e.getMessage(), e);
        }
    }
}
