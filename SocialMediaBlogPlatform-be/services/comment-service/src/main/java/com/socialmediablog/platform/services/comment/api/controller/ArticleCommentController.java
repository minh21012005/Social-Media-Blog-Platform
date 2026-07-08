package com.socialmediablog.platform.services.comment.api.controller;

import com.socialmediablog.platform.common.security.CurrentUser;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.comment.api.dto.CommentRequest;
import com.socialmediablog.platform.services.comment.api.dto.CommentResponse;
import com.socialmediablog.platform.services.comment.application.command.CreateCommentCommand;
import com.socialmediablog.platform.services.comment.application.port.in.CreateCommentUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.ListArticleCommentsUseCase;
import com.socialmediablog.platform.services.comment.application.query.ListArticleCommentsQuery;
import com.socialmediablog.platform.services.comment.application.query.CommentSortBy;
import com.socialmediablog.platform.services.comment.api.dto.CommentPageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/articles")
public class ArticleCommentController {

    private final CreateCommentUseCase createCommentUseCase;
    private final ListArticleCommentsUseCase listArticleCommentsUseCase;

    public ArticleCommentController(
            CreateCommentUseCase createCommentUseCase,
            ListArticleCommentsUseCase listArticleCommentsUseCase
    ) {
        this.createCommentUseCase = createCommentUseCase;
        this.listArticleCommentsUseCase = listArticleCommentsUseCase;
    }

    @GetMapping("/{articleId}/comments")
    public ApiResponse<CommentPageResponse> list(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "NEWEST") String sort
    ) {
        CommentSortBy sortBy = CommentSortBy.valueOf(sort.toUpperCase());
        UUID userId = currentUser != null ? currentUserId(currentUser) : null;
        return ApiResponse.success("Comments loaded", CommentPageResponse.from(
                listArticleCommentsUseCase.execute(new ListArticleCommentsQuery(articleId, page, size, sortBy, userId))
        ));
    }

    @PostMapping("/{articleId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> create(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID articleId,
            @Valid @RequestBody CommentRequest request
    ) {
        return ApiResponse.success("Comment created", CommentResponse.from(createCommentUseCase.execute(
                new CreateCommentCommand(
                        articleId,
                        currentUserId(currentUser),
                        request.content()
                )
        )));
    }

    private UUID currentUserId(CurrentUser currentUser) {
        return UUID.fromString(currentUser.id());
    }
}
