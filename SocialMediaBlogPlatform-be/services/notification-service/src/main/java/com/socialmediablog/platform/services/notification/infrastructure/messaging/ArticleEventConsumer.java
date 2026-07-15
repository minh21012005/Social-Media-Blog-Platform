package com.socialmediablog.platform.services.notification.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.notification.domain.aggregate.Notification;
import com.socialmediablog.platform.services.notification.domain.model.NotificationType;
import com.socialmediablog.platform.services.notification.domain.repository.NotificationRepository;
import com.socialmediablog.platform.services.notification.domain.vo.RecipientId;
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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void consume(String message) throws Exception {
        JsonNode payload = objectMapper.readTree(message);
        if (!"article.published".equals(payload.path("eventType").asText())) {
            return;
        }

        UUID eventId = UUID.fromString(payload.path("eventId").asText());
        if (processedEventRepository.existsById(eventId)) {
            return;
        }

        UUID articleId = UUID.fromString(payload.path("articleId").asText());
        UUID authorId = UUID.fromString(payload.path("authorId").asText());
        int page = 0;
        long totalNotified = 0;

        while (true) {
            ApiResponse<FollowerPage> response = followerServiceFeignClient.getFollowers(
                    authorId, page, FOLLOWER_PAGE_SIZE);
            if (response == null || response.data() == null) {
                throw new IllegalStateException("Could not load followers for " + authorId);
            }

            FollowerPage followerPage = response.data();
            List<FollowerItem> followers = followerPage.users();
            if (followers == null || followers.isEmpty()) {
                break;
            }

            for (FollowerItem follower : followers) {
                notificationRepository.save(Notification.create(
                        RecipientId.of(follower.userId()),
                        authorId,
                        NotificationType.ARTICLE_PUBLISHED,
                        "Article",
                        articleId,
                        "Tác giả bạn theo dõi vừa đăng bài mới",
                        null,
                        Instant.now()
                ));
                totalNotified++;
            }

            if (followers.size() < FOLLOWER_PAGE_SIZE) {
                break;
            }
            page++;
        }

        processedEventRepository.save(new JpaProcessedEventEntity(eventId));
        log.info("[ArticleEventConsumer] Notified {} followers for articleId={}", totalNotified, articleId);
    }
}
