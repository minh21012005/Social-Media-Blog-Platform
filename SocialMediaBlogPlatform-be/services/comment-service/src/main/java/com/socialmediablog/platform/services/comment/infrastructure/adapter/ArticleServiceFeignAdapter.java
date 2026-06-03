package com.socialmediablog.platform.services.comment.infrastructure.adapter;

import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.comment.application.port.out.ArticleCommentPolicyPort;
import com.socialmediablog.platform.services.comment.application.result.ArticleCommentPolicy;
import com.socialmediablog.platform.services.comment.infrastructure.client.ArticleCommentPolicyResponse;
import com.socialmediablog.platform.services.comment.infrastructure.client.ArticleServiceFeignClient;
import feign.FeignException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ArticleServiceFeignAdapter implements ArticleCommentPolicyPort {

    private final ArticleServiceFeignClient client;

    public ArticleServiceFeignAdapter(ArticleServiceFeignClient client) {
        this.client = client;
    }

    @Override
    public Optional<ArticleCommentPolicy> findByArticleId(UUID articleId) {
        try {
            ApiResponse<ArticleCommentPolicyResponse> response = client.getCommentPolicy(articleId);
            return Optional.ofNullable(response.data()).map(ArticleCommentPolicyResponse::toApplication);
        } catch (FeignException.NotFound exception) {
            return Optional.empty();
        }
    }
}
