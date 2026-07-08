package com.socialmediablog.platform.services.notification.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialmediablog.platform.services.notification.domain.aggregate.Notification;
import com.socialmediablog.platform.services.notification.domain.model.NotificationType;
import com.socialmediablog.platform.services.notification.domain.repository.NotificationRepository;
import com.socialmediablog.platform.services.notification.domain.vo.RecipientId;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.notification.infrastructure.entity.JpaProcessedEventEntity;
import com.socialmediablog.platform.services.notification.infrastructure.feign.FollowerItem;
import com.socialmediablog.platform.services.notification.infrastructure.feign.FollowerPage;
import com.socialmediablog.platform.services.notification.infrastructure.feign.FollowerServiceFeignClient;
import com.socialmediablog.platform.services.notification.infrastructure.persistence.SpringDataJpaProcessedEventRepository;
import java.time.Instant;
import java.util.List;
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
 * When an article is published:
 *   - Calls follower-service to get ALL followers of the author (via pagination).
 *   - Creates an ARTICLE_PUBLISHED notification for each follower.
 */
@Component
public class ArticleEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ArticleEventConsumer.class);
    private static final int FOLLOWER_PAGE_SIZE = 200;

    private final NotificationRepository notificationRepository;
    private final FollowerServiceFeignClient followerServiceFeignClient;
    private final ObjectMapper objectMapper;
    private final SpringDataJpaProcessedEventRepository processedEventRepository;

    public ArticleEventConsumer(
            NotificationRepository notificationRepository,
            FollowerServiceFeignClient followerServiceFeignClient,
            ObjectMapper objectMapper,
            SpringDataJpaProcessedEventRepository processedEventRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.followerServiceFeignClient = followerServiceFeignClient;
        this.objectMapper = objectMapper;
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(topics = "article.events", groupId = "notification-service")
    public void consume(String message) {
        try {
            JsonNode payload = objectMapper.readTree(message);
            String eventType = payload.path("eventType").asText();

            if (!"article.published".equals(eventType)) {
                return;
            }

            UUID eventId = UUID.fromString(payload.path("eventId").asText());
            if (processedEventRepository.existsById(eventId)) {
                log.info("[ArticleEventConsumer] Event {} already processed. Ignoring duplicate.", eventId);
                return;
            }

            // Đánh dấu là đã xử lý
            processedEventRepository.save(new JpaProcessedEventEntity(eventId));

            UUID articleId = UUID.fromString(payload.path("articleId").asText());
            UUID authorId = UUID.fromString(payload.path("authorId").asText());
            Instant now = Instant.now();

            log.info("[ArticleEventConsumer] Article published articleId={} authorId={}, notifying followers...", articleId, authorId);

            // Lấy tất cả followers theo phân trang
            int page = 0;
            long totalNotified = 0;

            while (true) {
                ApiResponse<FollowerPage> response = followerServiceFeignClient.getFollowers(authorId, page, FOLLOWER_PAGE_SIZE);
                FollowerPage followerPage = response.data();

                if (followerPage == null || followerPage.users() == null || followerPage.users().isEmpty()) {
                    break;
                }

                List<FollowerItem> followers = followerPage.users();
                for (FollowerItem follower : followers) {
                    Notification notification = Notification.create(
                            RecipientId.of(follower.userId()),   // người nhận = follower
                            authorId,                             // actor = tác giả bài viết
                            NotificationType.ARTICLE_PUBLISHED,
                            "Article",
                            articleId,
                            "Tác giả bạn theo dõi vừa đăng bài mới",
                            null,
                            now
                    );
                    notificationRepository.save(notification);
                }

                totalNotified += followers.size();
                log.debug("[ArticleEventConsumer] Notified {} followers (page {})", followers.size(), page);

                // Kiểm tra còn trang tiếp theo không
                if (followerPage.users().size() < FOLLOWER_PAGE_SIZE) {
                    break;
                }
                page++;
            }

            log.info("[ArticleEventConsumer] Done. Notified {} followers for articleId={}", totalNotified, articleId);

        } catch (Exception e) {
            log.error("[ArticleEventConsumer] Failed to process article.published event: {}", e.getMessage(), e);
        }
    }
}
