package com.socialmediablog.platform.services.notification.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.notification.domain.aggregate.Notification;
import com.socialmediablog.platform.services.notification.domain.model.NotificationType;
import com.socialmediablog.platform.services.notification.domain.repository.NotificationRepository;
import com.socialmediablog.platform.services.notification.domain.vo.RecipientId;
import com.socialmediablog.platform.services.notification.infrastructure.entity.JpaProcessedEventEntity;
import com.socialmediablog.platform.services.notification.infrastructure.feign.ArticleAuthorResponse;
import com.socialmediablog.platform.services.notification.infrastructure.feign.ArticleServiceFeignClient;
import com.socialmediablog.platform.services.notification.infrastructure.persistence.SpringDataJpaProcessedEventRepository;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InteractionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InteractionEventConsumer.class);

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;
    private final SpringDataJpaProcessedEventRepository processedEventRepository;
    private final ArticleServiceFeignClient articleServiceFeignClient;

    public InteractionEventConsumer(
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

    @KafkaListener(topics = "interaction.events", groupId = "notification-service")
    @Transactional
    public void consume(String message) throws Exception {
        JsonNode payload = objectMapper.readTree(message);
        if (!"interaction.recorded".equals(payload.path("eventType").asText())) {
            return;
        }

        UUID eventId = UUID.fromString(payload.path("eventId").asText());
        if (processedEventRepository.existsById(eventId)) {
            log.info("[InteractionEventConsumer] Event {} already processed. Ignoring duplicate.", eventId);
            return;
        }

        UUID articleId = UUID.fromString(payload.path("targetId").asText());
        UUID actorId = UUID.fromString(payload.path("userId").asText());
        ApiResponse<ArticleAuthorResponse> response = articleServiceFeignClient.getArticleAuthor(articleId);
        if (response == null || response.data() == null || response.data().authorId() == null) {
            throw new IllegalStateException("Could not resolve article author for " + articleId);
        }

        UUID articleAuthorId = response.data().authorId();
        if (!articleAuthorId.equals(actorId)) {
            RecipientId recipientId = RecipientId.of(articleAuthorId);
            boolean alreadyNotified = notificationRepository.existsByRecipientAndActorAndTypeAndTarget(
                    recipientId, actorId, NotificationType.ARTICLE_CLAPPED, articleId);
            if (!alreadyNotified) {
                notificationRepository.save(Notification.create(
                        recipientId,
                        actorId,
                        NotificationType.ARTICLE_CLAPPED,
                        "Article",
                        articleId,
                        "Bài viết của bạn nhận được lượt thích",
                        null,
                        Instant.now()
                ));
            }
        }

        processedEventRepository.save(new JpaProcessedEventEntity(eventId));
    }
}
