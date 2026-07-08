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

import com.socialmediablog.platform.services.notification.infrastructure.entity.JpaProcessedEventEntity;
import com.socialmediablog.platform.services.notification.infrastructure.persistence.SpringDataJpaProcessedEventRepository;

/**
 * Listens for follower.events Kafka topic.
 * Event payload (serialized from UserFollowedEvent / UserUnfollowedEvent):
 *   {"eventId":"...", "followerId":"...", "followedUserId":"...", "occurredAt":"...", "eventType":"user.followed"}
 */
@Component
public class FollowerEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(FollowerEventConsumer.class);

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;
    private final SpringDataJpaProcessedEventRepository processedEventRepository;

    public FollowerEventConsumer(
            NotificationRepository notificationRepository,
            ObjectMapper objectMapper,
            SpringDataJpaProcessedEventRepository processedEventRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(topics = "follower.events", groupId = "notification-service")
    public void consume(String message) {
        try {
            JsonNode payload = objectMapper.readTree(message);
            String eventType = payload.path("eventType").asText();

            if ("user.followed".equals(eventType)) {
                UUID eventId = UUID.fromString(payload.path("eventId").asText());
                if (processedEventRepository.existsById(eventId)) {
                    log.info("[FollowerEventConsumer] Event {} already processed. Ignoring duplicate.", eventId);
                    return;
                }
                processedEventRepository.save(new JpaProcessedEventEntity(eventId));

                UUID followerId = UUID.fromString(payload.path("followerId").asText());
                UUID followedUserId = UUID.fromString(payload.path("followedUserId").asText());
                Instant now = Instant.now();

                Notification notification = Notification.create(
                        RecipientId.of(followedUserId),
                        followerId,
                        NotificationType.USER_FOLLOWED,
                        "User",
                        followerId,
                        "Bạn có người theo dõi mới",
                        null,
                        now
                );
                notificationRepository.save(notification);
                log.info("[FollowerEventConsumer] Saved USER_FOLLOWED notification: recipient={} actor={}", followedUserId, followerId);
            } else if ("user.follow-requested".equals(eventType)) {
                UUID eventId = UUID.fromString(payload.path("eventId").asText());
                if (processedEventRepository.existsById(eventId)) {
                    log.info("[FollowerEventConsumer] Event {} already processed. Ignoring duplicate.", eventId);
                    return;
                }
                processedEventRepository.save(new JpaProcessedEventEntity(eventId));

                UUID followerId = UUID.fromString(payload.path("followerId").asText());
                UUID followedUserId = UUID.fromString(payload.path("followedUserId").asText());
                Instant now = Instant.now();

                Notification notification = Notification.create(
                        RecipientId.of(followedUserId),
                        followerId,
                        NotificationType.USER_FOLLOW_REQUESTED,
                        "User",
                        followerId,
                        "Yêu cầu theo dõi mới",
                        "muốn theo dõi bạn",
                        now
                );
                notificationRepository.save(notification);
                log.info("[FollowerEventConsumer] Saved USER_FOLLOW_REQUESTED notification: recipient={} actor={}", followedUserId, followerId);
            } else if ("user.follow-accepted".equals(eventType)) {
                UUID eventId = UUID.fromString(payload.path("eventId").asText());
                if (processedEventRepository.existsById(eventId)) {
                    log.info("[FollowerEventConsumer] Event {} already processed. Ignoring duplicate.", eventId);
                    return;
                }
                processedEventRepository.save(new JpaProcessedEventEntity(eventId));

                UUID followerId = UUID.fromString(payload.path("followerId").asText());
                UUID followedUserId = UUID.fromString(payload.path("followedUserId").asText());
                Instant now = Instant.now();

                Notification notification = Notification.create(
                        RecipientId.of(followerId),
                        followedUserId,
                        NotificationType.USER_FOLLOW_ACCEPTED,
                        "User",
                        followedUserId,
                        "Yêu cầu theo dõi được chấp nhận",
                        "đã chấp nhận yêu cầu theo dõi của bạn",
                        now
                );
                notificationRepository.save(notification);
                log.info("[FollowerEventConsumer] Saved USER_FOLLOW_ACCEPTED notification: recipient={} actor={}", followerId, followedUserId);
            }

        } catch (Exception e) {
            log.error("[FollowerEventConsumer] Failed to process follower event: {}", e.getMessage(), e);
        }
    }
}
