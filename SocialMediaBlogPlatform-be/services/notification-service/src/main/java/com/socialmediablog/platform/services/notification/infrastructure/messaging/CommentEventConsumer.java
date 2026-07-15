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
    @Transactional
    public void consume(String message) throws Exception {
        JsonNode payload = objectMapper.readTree(message);
        String eventType = payload.path("eventType").asText();
        if (!eventType.equals("comment.created")
                && !eventType.equals("comment.replied")
                && !eventType.equals("comment.clapped")) {
            return;
        }

        UUID eventId = requiredUuid(payload, "eventId");
        if (processedEventRepository.existsById(eventId)) {
            log.info("[CommentEventConsumer] Event {} already processed. Ignoring duplicate.", eventId);
            return;
        }

        switch (eventType) {
            case "comment.created" -> handleCommentCreated(payload);
            case "comment.replied" -> handleCommentReplied(payload);
            case "comment.clapped" -> handleCommentClapped(payload);
            default -> throw new IllegalStateException("Unsupported comment event: " + eventType);
        }

        processedEventRepository.save(new JpaProcessedEventEntity(eventId));
    }

    private void handleCommentCreated(JsonNode payload) {
        UUID articleId = requiredUuid(payload, "articleId");
        UUID commentAuthorId = requiredUuid(payload, "authorId");
        UUID articleAuthorId = resolveArticleAuthor(articleId);

        if (!articleAuthorId.equals(commentAuthorId)) {
            saveNotification(
                    articleAuthorId,
                    commentAuthorId,
                    NotificationType.COMMENT_CREATED,
                    "Article",
                    articleId,
                    "Bài viết của bạn có bình luận mới"
            );
        }
    }

    private void handleCommentReplied(JsonNode payload) {
        UUID replyId = requiredUuid(payload, "replyId");
        UUID parentCommentId = requiredUuid(payload, "parentCommentId");
        UUID articleId = requiredUuid(payload, "articleId");
        UUID replyAuthorId = requiredUuid(payload, "authorId");
        UUID parentAuthorId = requiredUuid(payload, "parentAuthorId");

        if (!parentAuthorId.equals(replyAuthorId)) {
            saveNotification(
                    parentAuthorId,
                    replyAuthorId,
                    NotificationType.COMMENT_REPLIED,
                    "Comment",
                    replyId,
                    "Bình luận của bạn có phản hồi mới"
            );
        }

        UUID articleAuthorId = resolveArticleAuthor(articleId);
        if (!articleAuthorId.equals(replyAuthorId)
                && !articleAuthorId.equals(parentAuthorId)) {
            saveNotification(
                    articleAuthorId,
                    replyAuthorId,
                    NotificationType.COMMENT_CREATED,
                    "Article",
                    articleId,
                    "Bài viết của bạn có phản hồi mới"
            );
        }
        log.info("[CommentEventConsumer] Processed COMMENT_REPLIED replyId={}", replyId);
    }

    private void handleCommentClapped(JsonNode payload) {
        UUID commentId = requiredUuid(payload, "commentId");
        UUID commentAuthorId = requiredUuid(payload, "commentAuthorId");
        UUID actorId = requiredUuid(payload, "userId");

        if (commentAuthorId.equals(actorId)) {
            return;
        }

        RecipientId recipientId = RecipientId.of(commentAuthorId);
        boolean alreadyNotified = notificationRepository.existsByRecipientAndActorAndTypeAndTarget(
                recipientId, actorId, NotificationType.COMMENT_CLAPPED, commentId);
        if (!alreadyNotified) {
            saveNotification(
                    commentAuthorId,
                    actorId,
                    NotificationType.COMMENT_CLAPPED,
                    "Comment",
                    commentId,
                    "Bình luận của bạn nhận được lượt thích"
            );
        }
    }

    private UUID resolveArticleAuthor(UUID articleId) {
        ApiResponse<ArticleAuthorResponse> response = articleServiceFeignClient.getArticleAuthor(articleId);
        if (response == null || response.data() == null || response.data().authorId() == null) {
            throw new IllegalStateException("Could not resolve article author for " + articleId);
        }
        return response.data().authorId();
    }

    private void saveNotification(
            UUID recipientId,
            UUID actorId,
            NotificationType type,
            String subjectType,
            UUID subjectId,
            String title
    ) {
        notificationRepository.save(Notification.create(
                RecipientId.of(recipientId), actorId, type, subjectType, subjectId, title, null, Instant.now()));
    }

    private UUID requiredUuid(JsonNode payload, String field) {
        String value = payload.path(field).asText();
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing UUID field: " + field);
        }
        return UUID.fromString(value);
    }
}
