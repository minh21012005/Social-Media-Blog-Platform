package com.socialmediablog.platform.services.comment.api.controller;

import com.socialmediablog.platform.services.comment.api.dto.CommentCountResponse;
import com.socialmediablog.platform.services.comment.application.port.in.CountArticleCommentsUseCase;
import com.socialmediablog.platform.services.comment.application.query.CountArticleCommentsQuery;
import com.socialmediablog.platform.common.web.ApiResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/articles")
public class CommentCountController {

    private final CountArticleCommentsUseCase countArticleCommentsUseCase;

    public CommentCountController(CountArticleCommentsUseCase countArticleCommentsUseCase) {
        this.countArticleCommentsUseCase = countArticleCommentsUseCase;
    }

    @GetMapping("/{articleId}/comments/count")
    public ApiResponse<CommentCountResponse> countArticleComments(@PathVariable UUID articleId) {
        long count = countArticleCommentsUseCase.execute(new CountArticleCommentsQuery(articleId));
        return ApiResponse.success(new CommentCountResponse(articleId, count));
    }
}
