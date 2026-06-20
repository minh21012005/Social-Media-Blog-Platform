package com.socialmediablog.platform.services.notification.infrastructure.feign;

import com.socialmediablog.platform.common.web.ApiResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "article-service", path = "/internal/articles")
public interface ArticleServiceFeignClient {

    @GetMapping("/{articleId}/author")
    ApiResponse<ArticleAuthorResponse> getArticleAuthor(@PathVariable("articleId") UUID articleId);
}
