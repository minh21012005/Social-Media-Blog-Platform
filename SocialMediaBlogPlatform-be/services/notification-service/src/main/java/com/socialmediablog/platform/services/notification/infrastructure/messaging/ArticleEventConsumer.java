package com.socialmediablog.platform.services.notification.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialmediablog.platform.services.notification.domain.aggregate.Notification;
import com.socialmediablog.platform.services.notification.domain.model.NotificationType;
import com.socialmediablog.platform.services.notification.domain.repository.NotificationRepository;
import com.socialmediablog.platform.services.notification.domain.vo.RecipientId;
import com.socialmediablog.platform.services.notification.infrastructure.feign.ArticleServiceFeignClient;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens for article.events Kafka topic.
 * Event payload (serialized from ArticlePublishedEvent):
 *   {"eventId":"...", "articleId":"...", "authorId":"...", "occurredAt":"...", "eventType":"article.published"}
 *
 * When an article is published, we notify all followers of the author.
 * We query follower-service to get the list of followers.
 */
@Component
public class ArticleEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ArticleEventConsumer.class);

    private final NotificationRepository notificationRepository;
    private final ArticleServiceFeignClient articleServiceFeignClient;
    private final ObjectMapper objectMapper;

    public ArticleEventConsumer(
            NotificationRepository notificationRepository,
            ArticleServiceFeignClient articleServiceFeignClient,
            ObjectMapper objectMapper
    ) {
        this.notificationRepository = notificationRepository;
        this.articleServiceFeignClient = articleServiceFeignClient;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "article.events", groupId = "notification-service")
    public void consume(String message) {
        try {
            JsonNode payload = objectMapper.readTree(message);
            String eventType = payload.path("eventType").asText();

            if (!"article.published".equals(eventType)) {
                return;
            }

            UUID articleId = UUID.fromString(payload.path("articleId").asText());
            UUID authorId = UUID.fromString(payload.path("authorId").asText());
            Instant now = Instant.now();

            // Lấy danh sách follower của tác giả để gửi thông báo
            // TODO: Gọi follower-service qua Feign để lấy followers của authorId và tạo notification cho mỗi người
            // Hiện tại tạo notification cho chính authorId để xác nhận luồng hoạt động
            Notification notification = Notification.create(
                    RecipientId.of(authorId),
                    authorId,
                    NotificationType.ARTICLE_PUBLISHED,
                    "Article",
                    articleId,
                    "Bài viết của bạn đã được xuất bản",
                    null,
                    now
            );
            notificationRepository.save(notification);
            log.info("[NotificationConsumer] Saved ARTICLE_PUBLISHED notification: articleId={} authorId={}", articleId, authorId);

        } catch (Exception e) {
            log.error("[NotificationConsumer] Failed to process article event: {}", e.getMessage(), e);
        }
    }
}
