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

import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.notification.infrastructure.entity.JpaProcessedEventEntity;
import com.socialmediablog.platform.services.notification.infrastructure.feign.ArticleAuthorResponse;
import com.socialmediablog.platform.services.notification.infrastructure.feign.ArticleServiceFeignClient;
import com.socialmediablog.platform.services.notification.infrastructure.persistence.SpringDataJpaProcessedEventRepository;

/**
 * Listens for comment.events Kafka topic.
 * Event payload (serialized from CommentCreatedEvent):
 *   {"eventId":"...", "commentId":"...", "articleId":"...", "authorId":"...", "occurredAt":"...", "eventType":"comment.created"}
 */
@Component
public class CommentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(CommentEventConsumer.class);

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;
    private final SpringDataJpaProcessedEventRepository processedEventRepository;
    private final ArticleServiceFeignClient articleServiceFeignClient;

    public CommentEventConsumer(
            NotificationRepository notificationRepository,
            ObjectMapper objectMapper,
            SpringDataJpaProcessedEventRepository processedEventRepository,
            ArticleServiceFeignClient articleServiceFeignClient
    ) {
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
        this.processedEventRepository = processedEventRepository;
        this.articleServiceFeignClient = articleServiceFeignClient;
    }

    @KafkaListener(topics = "comment.events", groupId = "notification-service")
    public void consume(String message) {
        try {
            JsonNode payload = objectMapper.readTree(message);
            String eventType = payload.path("eventType").asText();

            if (!"comment.created".equals(eventType)) {
                return;
            }

            UUID eventId = UUID.fromString(payload.path("eventId").asText());
            if (processedEventRepository.existsById(eventId)) {
                log.info("[CommentEventConsumer] Event {} already processed. Ignoring duplicate.", eventId);
                return;
            }
            processedEventRepository.save(new JpaProcessedEventEntity(eventId));

            UUID articleId = UUID.fromString(payload.path("articleId").asText());
            UUID commentAuthorId = UUID.fromString(payload.path("authorId").asText());
            Instant now = Instant.now();

            // Call article-service via Feign to get article.authorId
            ApiResponse<ArticleAuthorResponse> response = articleServiceFeignClient.getArticleAuthor(articleId);
            if (response != null && response.data() != null) {
                UUID articleAuthorId = response.data().authorId();

                // Only notify if someone else commented on the article
                if (!articleAuthorId.equals(commentAuthorId)) {
                    Notification notification = Notification.create(
                            RecipientId.of(articleAuthorId),
                            commentAuthorId,
                            NotificationType.COMMENT_CREATED,
                            "Article",
                            articleId,
                            "Bài viết của bạn có bình luận mới",
                            null,
                            now
                    );
                    notificationRepository.save(notification);
                    log.info("[CommentEventConsumer] Saved COMMENT_CREATED notification for articleId={}", articleId);
                } else {
                    log.debug("[CommentEventConsumer] User commented on their own article. No notification sent.");
                }
            }

        } catch (Exception e) {
            log.error("[CommentEventConsumer] Failed to process comment event: {}", e.getMessage(), e);
        }
    }
}
