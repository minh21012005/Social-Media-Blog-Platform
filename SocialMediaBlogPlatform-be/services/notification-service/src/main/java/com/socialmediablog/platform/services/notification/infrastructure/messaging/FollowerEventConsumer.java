package com.socialmediablog.platform.services.notification.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialmediablog.platform.services.notification.domain.aggregate.Notification;
import com.socialmediablog.platform.services.notification.domain.model.NotificationType;
import com.socialmediablog.platform.services.notification.domain.repository.NotificationRepository;
import com.socialmediablog.platform.services.notification.domain.vo.RecipientId;
import com.socialmediablog.platform.services.notification.infrastructure.entity.JpaProcessedEventEntity;
import com.socialmediablog.platform.services.notification.infrastructure.persistence.SpringDataJpaProcessedEventRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FollowerEventConsumer {

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
    @Transactional
    public void consume(String message) throws Exception {
        JsonNode payload = objectMapper.readTree(message);
        String eventType = payload.path("eventType").asText();
        NotificationType notificationType = switch (eventType) {
            case "user.followed" -> NotificationType.USER_FOLLOWED;
            case "user.follow-requested" -> NotificationType.USER_FOLLOW_REQUESTED;
            case "user.follow-accepted" -> NotificationType.USER_FOLLOW_ACCEPTED;
            default -> null;
        };
        if (notificationType == null) {
            return;
        }

        UUID eventId = UUID.fromString(payload.path("eventId").asText());
        if (processedEventRepository.existsById(eventId)) {
            return;
        }

        UUID followerId = UUID.fromString(payload.path("followerId").asText());
        UUID followedUserId = UUID.fromString(payload.path("followedUserId").asText());
        UUID recipientId = notificationType == NotificationType.USER_FOLLOW_ACCEPTED
                ? followerId : followedUserId;
        UUID actorId = notificationType == NotificationType.USER_FOLLOW_ACCEPTED
                ? followedUserId : followerId;

            notificationRepository.save(Notification.create(
                    RecipientId.of(recipientId),
                    actorId,
                    notificationType,
                    "User",
                    actorId,
                    title(notificationType),
                    null,
                    Instant.now()
            ));

        processedEventRepository.save(new JpaProcessedEventEntity(eventId));
    }

    private String title(NotificationType type) {
        return switch (type) {
            case USER_FOLLOWED -> "Bạn có người theo dõi mới";
            case USER_FOLLOW_REQUESTED -> "Yêu cầu theo dõi mới";
            case USER_FOLLOW_ACCEPTED -> "Yêu cầu theo dõi được chấp nhận";
            default -> throw new IllegalArgumentException("Unsupported follower notification type: " + type);
        };
    }
}
