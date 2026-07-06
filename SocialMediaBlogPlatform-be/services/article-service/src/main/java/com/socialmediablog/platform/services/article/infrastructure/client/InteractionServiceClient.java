package com.socialmediablog.platform.services.article.infrastructure.client;

import com.socialmediablog.platform.common.web.ApiResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "interaction-service")
public interface InteractionServiceClient {

    @GetMapping("/api/v1/interactions/{articleId}/likes")
    ApiResponse<Long> countArticleLikes(@PathVariable UUID articleId);
}

