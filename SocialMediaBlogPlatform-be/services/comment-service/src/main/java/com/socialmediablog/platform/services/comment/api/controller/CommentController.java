package com.socialmediablog.platform.services.comment.api.controller;

import com.socialmediablog.platform.common.security.CurrentUser;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.comment.api.dto.CommentRequest;
import com.socialmediablog.platform.services.comment.api.dto.CommentResponse;
import com.socialmediablog.platform.services.comment.api.dto.ServiceStatusResponse;
import com.socialmediablog.platform.services.comment.application.command.EditCommentCommand;
import com.socialmediablog.platform.services.comment.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.comment.application.port.in.EditCommentUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.GetServiceStatusUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    public CommentController(
            GetServiceStatusUseCase getServiceStatusUseCase,
            EditCommentUseCase editCommentUseCase
    ) {
        this.getServiceStatusUseCase = getServiceStatusUseCase;
        this.editCommentUseCase = editCommentUseCase;
    }

    @GetMapping("/status")
    public ApiResponse<ServiceStatusResponse> status(@AuthenticationPrincipal CurrentUser currentUser) {
        return ApiResponse.success(ServiceStatusResponse.from(getServiceStatusUseCase.execute(
                new GetServiceStatusCommand(currentUser.id())
        )));
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

    private UUID currentUserId(CurrentUser currentUser) {
        return UUID.fromString(currentUser.id());
    }
}
