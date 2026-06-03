package com.socialmediablog.platform.services.article.api.controller;

import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.common.web.error.ErrorCode;
import com.socialmediablog.platform.services.article.api.dto.ArticleCommentPolicyResponse;
import com.socialmediablog.platform.services.article.domain.repository.ArticleRepository;
import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/articles")
public class InternalArticleController {

    private final ArticleRepository articleRepository;

    public InternalArticleController(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @GetMapping("/{articleId}/comment-policy")
    public ResponseEntity<ApiResponse<ArticleCommentPolicyResponse>> commentPolicy(@PathVariable UUID articleId) {
        return articleRepository.findById(ArticleId.of(articleId))
                .map(ArticleCommentPolicyResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success("Article comment policy loaded", response)))
                .orElseGet(() -> ResponseEntity.status(404).body(new ApiResponse<>(
                        404,
                        ErrorCode.NOT_FOUND.defaultMessage(),
                        null
                )));
    }
}
