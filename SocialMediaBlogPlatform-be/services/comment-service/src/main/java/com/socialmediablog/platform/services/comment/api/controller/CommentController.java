package com.socialmediablog.platform.services.comment.api.controller;

import com.socialmediablog.platform.common.security.CurrentUser;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.comment.api.dto.CommentRequest;
import com.socialmediablog.platform.services.comment.api.dto.CommentResponse;
import com.socialmediablog.platform.services.comment.api.dto.ServiceStatusResponse;
import com.socialmediablog.platform.services.comment.application.command.DeleteCommentCommand;
import com.socialmediablog.platform.services.comment.application.command.EditCommentCommand;
import com.socialmediablog.platform.services.comment.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.comment.application.port.in.DeleteCommentUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.EditCommentUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.ListCommentRepliesUseCase;
import com.socialmediablog.platform.services.comment.application.query.ListCommentRepliesQuery;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final GetServiceStatusUseCase getServiceStatusUseCase;
    private final EditCommentUseCase editCommentUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;
    private final ListCommentRepliesUseCase listCommentRepliesUseCase;

    public CommentController(
            GetServiceStatusUseCase getServiceStatusUseCase,
            EditCommentUseCase editCommentUseCase,
            DeleteCommentUseCase deleteCommentUseCase,
            ListCommentRepliesUseCase listCommentRepliesUseCase
    ) {
        this.getServiceStatusUseCase = getServiceStatusUseCase;
        this.editCommentUseCase = editCommentUseCase;
        this.deleteCommentUseCase = deleteCommentUseCase;
        this.listCommentRepliesUseCase = listCommentRepliesUseCase;
    }

    @GetMapping("/status")
    public ApiResponse<ServiceStatusResponse> status(@AuthenticationPrincipal CurrentUser currentUser) {
        return ApiResponse.success(ServiceStatusResponse.from(getServiceStatusUseCase.execute(
                new GetServiceStatusCommand(currentUser.id())
        )));
    }

    @GetMapping("/{commentId}/replies")
    public ApiResponse<List<CommentResponse>> replies(@PathVariable UUID commentId) {
        return ApiResponse.success("Replies loaded", listCommentRepliesUseCase.execute(
                        new ListCommentRepliesQuery(commentId)
                ).stream()
                .map(CommentResponse::from)
                .toList());
    }

    @PatchMapping("/{commentId}")
    public ApiResponse<CommentResponse> edit(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentRequest request
    ) {
        return ApiResponse.success("Comment updated", CommentResponse.from(editCommentUseCase.execute(
                new EditCommentCommand(
                        commentId,
                        currentUserId(currentUser),
                        request.content()
                )
        )));
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID commentId
    ) {
        deleteCommentUseCase.execute(new DeleteCommentCommand(
                commentId,
                currentUserId(currentUser)
        ));
    }

    private UUID currentUserId(CurrentUser currentUser) {
        return UUID.fromString(currentUser.id());
    }
}
