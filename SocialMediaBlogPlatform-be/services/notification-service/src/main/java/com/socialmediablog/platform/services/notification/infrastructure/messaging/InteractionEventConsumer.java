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
 * Listens for interaction.events Kafka topic.
 * Event payload (serialized from InteractionRecordedEvent):
 *   {"eventId":"...", "interactionId":"...", "targetId":"...", "userId":"...", "occurredAt":"...", "eventType":"interaction.recorded"}
 *
 * NOTE: InteractionRecordedEvent does not contain the target (article/comment) owner's ID.
 * TODO: Call article-service or comment-service to resolve owner of targetId and send notification.
 */
@Component
public class InteractionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InteractionEventConsumer.class);

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    public InteractionEventConsumer(
            NotificationRepository notificationRepository,
            ObjectMapper objectMapper
    ) {
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "interaction.events", groupId = "notification-service")
    public void consume(String message) {
        try {
            JsonNode payload = objectMapper.readTree(message);
            String eventType = payload.path("eventType").asText();

            if (!"interaction.recorded".equals(eventType)) {
                return;
            }

            UUID targetId = UUID.fromString(payload.path("targetId").asText());
            UUID userId = UUID.fromString(payload.path("userId").asText());
            Instant now = Instant.now();

            // TODO: Resolve target owner from article-service/comment-service
            // For now: create an ARTICLE_CLAPPED placeholder notification
            Notification notification = Notification.create(
                    RecipientId.of(userId), // Placeholder: should be target owner
                    userId,
                    NotificationType.ARTICLE_CLAPPED,
                    "Article",
                    targetId,
                    "Bài viết của bạn nhận được lượt thích",
                    null,
                    now
            );
            notificationRepository.save(notification);
            log.info("[NotificationConsumer] Saved ARTICLE_CLAPPED notification: targetId={} userId={}", targetId, userId);

        } catch (Exception e) {
            log.error("[NotificationConsumer] Failed to process interaction event: {}", e.getMessage(), e);
        }
    }
}
