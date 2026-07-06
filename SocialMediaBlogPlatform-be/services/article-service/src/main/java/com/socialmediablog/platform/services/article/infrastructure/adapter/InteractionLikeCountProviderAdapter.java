package com.socialmediablog.platform.services.article.infrastructure.adapter;

import com.socialmediablog.platform.services.article.application.port.out.ArticleLikeCountProvider;
import com.socialmediablog.platform.services.article.infrastructure.client.InteractionServiceClient;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InteractionLikeCountProviderAdapter implements ArticleLikeCountProvider {

    private static final Logger log = LoggerFactory.getLogger(InteractionLikeCountProviderAdapter.class);

    private final InteractionServiceClient interactionServiceClient;

    public InteractionLikeCountProviderAdapter(InteractionServiceClient interactionServiceClient) {
        this.interactionServiceClient = interactionServiceClient;
    }

    @Override
    public long countLikes(UUID articleId) {
        try {
            var response = interactionServiceClient.countArticleLikes(articleId);
            if (response == null || response.data() == null) {
                log.info("[InteractionLikeCountProviderAdapter] Like count for article {} is 0 (response or data is null)", articleId);
                return 0L;
            }
            long likeCount = response.data();
            log.info("[InteractionLikeCountProviderAdapter] Like count for article {} is {}", articleId, likeCount);
            return likeCount;
        } catch (Exception e) {
            log.warn("[InteractionLikeCountProviderAdapter] Failed to load like count for article {}: {}", articleId, e.getMessage());
            return 0L;
        }
    }
}
