package com.socialmediablog.platform.services.article.api;

import com.socialmediablog.platform.common.web.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/articles")
public class ArticleController {

    @GetMapping("/status")
    public ApiResponse<String> status() {
        return ApiResponse.success("article-service");
    }
}
