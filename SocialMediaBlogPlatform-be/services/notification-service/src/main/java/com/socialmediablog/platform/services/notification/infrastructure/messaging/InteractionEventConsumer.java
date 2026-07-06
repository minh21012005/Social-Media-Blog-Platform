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
import com.socialmediablog.platform.services.notification.infrastructure.feign.UserPublicProfileResponse;
import com.socialmediablog.platform.services.notification.infrastructure.feign.UserServiceFeignClient;
import com.socialmediablog.platform.services.notification.infrastructure.persistence.SpringDataJpaProcessedEventRepository;

/**
 * Listens for interaction.events Kafka topic.
 * Event payload (serialized from InteractionRecordedEvent):
 *   {"eventId":"...", "interactionId":"...", "targetId":"...", "userId":"...", "occurredAt":"...", "eventType":"interaction.recorded"}
 */
@Component
public class InteractionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InteractionEventConsumer.class);

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;
    private final SpringDataJpaProcessedEventRepository processedEventRepository;
    private final ArticleServiceFeignClient articleServiceFeignClient;
    private final UserServiceFeignClient userServiceFeignClient;

    public InteractionEventConsumer(
            NotificationRepository notificationRepository,
            ObjectMapper objectMapper,
            SpringDataJpaProcessedEventRepository processedEventRepository,
            ArticleServiceFeignClient articleServiceFeignClient,
            UserServiceFeignClient userServiceFeignClient
    ) {
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
        this.processedEventRepository = processedEventRepository;
        this.articleServiceFeignClient = articleServiceFeignClient;
        this.userServiceFeignClient = userServiceFeignClient;
    }

    @KafkaListener(topics = "interaction.events", groupId = "notification-service")
    public void consume(String message) {
        try {
            JsonNode payload = objectMapper.readTree(message);
            String eventType = payload.path("eventType").asText();

            if (!"interaction.recorded".equals(eventType)) {
                return;
            }

            UUID eventId = UUID.fromString(payload.path("eventId").asText());
            if (processedEventRepository.existsById(eventId)) {
                log.info("[InteractionEventConsumer] Event {} already processed. Ignoring duplicate.", eventId);
                return;
            }
            processedEventRepository.save(new JpaProcessedEventEntity(eventId));

            UUID targetId = UUID.fromString(payload.path("targetId").asText());
            UUID userId = UUID.fromString(payload.path("userId").asText()); // The user who interacted
            Instant now = Instant.now();
            String actorDisplayName = resolveActorDisplayName(userId);

            // Try to resolve targetId as an article
            try {
                ApiResponse<ArticleAuthorResponse> response = articleServiceFeignClient.getArticleAuthor(targetId);
                if (response != null && response.data() != null) {
                    UUID articleAuthorId = response.data().authorId();

                    if (!articleAuthorId.equals(userId)) {
                        Notification notification = Notification.create(
                                RecipientId.of(articleAuthorId),
                                userId,
                                NotificationType.ARTICLE_CLAPPED,
                                "Article",
                                targetId,
                            actorDisplayName + " vừa thích bài viết của bạn",
                            "Nhấn để xem bài viết được yêu thích.",
                                now
                        );
                        notificationRepository.save(notification);
                        log.info("[InteractionEventConsumer] Saved ARTICLE_CLAPPED notification for articleId={}", targetId);
                    }
                }
            } catch (Exception e) {
                // If article is not found, target might be a comment. Ignore for now.
                log.debug("[InteractionEventConsumer] Target {} not found as an article. It might be a comment.", targetId);
            }

        } catch (Exception e) {
            log.error("[InteractionEventConsumer] Failed to process interaction event: {}", e.getMessage(), e);
        }
    }

    private String resolveActorDisplayName(UUID actorId) {
        try {
            ApiResponse<UserPublicProfileResponse> response = userServiceFeignClient.getPublicUser(actorId);
            UserPublicProfileResponse actor = response == null ? null : response.data();
            if (actor == null) {
                return "Một người dùng";
            }
            if (actor.displayName() != null && !actor.displayName().isBlank()) {
                return actor.displayName();
            }
            if (actor.username() != null && !actor.username().isBlank()) {
                return actor.username();
            }
        } catch (Exception exception) {
            log.debug("[InteractionEventConsumer] Could not resolve actor profile for {}", actorId, exception);
        }
        return "Một người dùng";
    }
}
