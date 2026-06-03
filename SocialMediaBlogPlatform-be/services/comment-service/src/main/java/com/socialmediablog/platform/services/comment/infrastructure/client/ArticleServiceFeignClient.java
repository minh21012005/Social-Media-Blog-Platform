package com.socialmediablog.platform.services.comment.infrastructure.client;

import com.socialmediablog.platform.common.web.ApiResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "article-service")
public interface ArticleServiceFeignClient {

    @GetMapping("/internal/articles/{articleId}/comment-policy")
    ApiResponse<ArticleCommentPolicyResponse> getCommentPolicy(@PathVariable UUID articleId);
}
