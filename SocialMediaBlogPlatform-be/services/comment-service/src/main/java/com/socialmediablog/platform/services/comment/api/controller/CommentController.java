package com.socialmediablog.platform.services.comment.api.controller;

import com.socialmediablog.platform.common.security.CurrentUser;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.comment.api.dto.CommentRequest;
import com.socialmediablog.platform.services.comment.api.dto.CommentResponse;
import com.socialmediablog.platform.services.comment.api.dto.ServiceStatusResponse;
import com.socialmediablog.platform.services.comment.application.command.DeleteCommentCommand;
import com.socialmediablog.platform.services.comment.application.command.EditCommentCommand;
import com.socialmediablog.platform.services.comment.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.comment.application.command.ReplyCommentCommand;
import com.socialmediablog.platform.services.comment.application.port.in.DeleteCommentUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.EditCommentUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.ListCommentRepliesUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.ReplyCommentUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.PinCommentUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.UnpinCommentUseCase;
import com.socialmediablog.platform.services.comment.application.command.PinCommentCommand;
import com.socialmediablog.platform.services.comment.application.command.UnpinCommentCommand;
import com.socialmediablog.platform.services.comment.application.command.ClapCommentCommand;
import com.socialmediablog.platform.services.comment.application.command.UndoClapCommentCommand;
import com.socialmediablog.platform.services.comment.application.port.in.ClapCommentUseCase;
import com.socialmediablog.platform.services.comment.application.port.in.UndoClapCommentUseCase;
import com.socialmediablog.platform.services.comment.application.query.ListCommentRepliesQuery;
import com.socialmediablog.platform.services.comment.api.dto.CommentPageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
        private final ReplyCommentUseCase replyCommentUseCase;
        private final PinCommentUseCase pinCommentUseCase;
        private final UnpinCommentUseCase unpinCommentUseCase;
        private final ClapCommentUseCase clapCommentUseCase;
        private final UndoClapCommentUseCase undoClapCommentUseCase;

        public CommentController(
                        GetServiceStatusUseCase getServiceStatusUseCase,
                        EditCommentUseCase editCommentUseCase,
                        DeleteCommentUseCase deleteCommentUseCase,
                        ListCommentRepliesUseCase listCommentRepliesUseCase,
                        ReplyCommentUseCase replyCommentUseCase,
                        PinCommentUseCase pinCommentUseCase,
                        UnpinCommentUseCase unpinCommentUseCase,
                        ClapCommentUseCase clapCommentUseCase,
                        UndoClapCommentUseCase undoClapCommentUseCase) {
                this.getServiceStatusUseCase = getServiceStatusUseCase;
                this.editCommentUseCase = editCommentUseCase;
                this.deleteCommentUseCase = deleteCommentUseCase;
                this.listCommentRepliesUseCase = listCommentRepliesUseCase;
                this.replyCommentUseCase = replyCommentUseCase;
                this.pinCommentUseCase = pinCommentUseCase;
                this.unpinCommentUseCase = unpinCommentUseCase;
                this.clapCommentUseCase = clapCommentUseCase;
                this.undoClapCommentUseCase = undoClapCommentUseCase;
        }

        @GetMapping("/status")
        public ApiResponse<ServiceStatusResponse> status(@AuthenticationPrincipal CurrentUser currentUser) {
                return ApiResponse.success(ServiceStatusResponse.from(getServiceStatusUseCase.execute(
                                new GetServiceStatusCommand(currentUser.id()))));
        }

        @GetMapping("/{commentId}/replies")
        public ApiResponse<CommentPageResponse> replies(
                        @AuthenticationPrincipal CurrentUser currentUser,
                        @PathVariable UUID commentId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {
                UUID userId = currentUser != null ? currentUserId(currentUser) : null;
                return ApiResponse.success("Replies loaded", CommentPageResponse.from(
                                listCommentRepliesUseCase
                                                .execute(new ListCommentRepliesQuery(commentId, page, size, userId))));
        }

        @PostMapping("/{commentId}/replies")
        @ResponseStatus(HttpStatus.CREATED)
        public ApiResponse<CommentResponse> reply(
                        @AuthenticationPrincipal CurrentUser currentUser,
                        @PathVariable UUID commentId,
                        @Valid @RequestBody CommentRequest request) {
                return ApiResponse.success("Reply created", CommentResponse.from(replyCommentUseCase.execute(
                                new ReplyCommentCommand(
                                                commentId,
                                                currentUserId(currentUser),
                                                request.content()))));
        }

        @PatchMapping("/{commentId}")
        public ApiResponse<CommentResponse> edit(
                        @AuthenticationPrincipal CurrentUser currentUser,
                        @PathVariable UUID commentId,
                        @Valid @RequestBody CommentRequest request) {
                return ApiResponse.success("Comment updated", CommentResponse.from(editCommentUseCase.execute(
                                new EditCommentCommand(
                                                commentId,
                                                currentUserId(currentUser),
                                                request.content()))));
        }

        @DeleteMapping("/{commentId}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void delete(
                        @AuthenticationPrincipal CurrentUser currentUser,
                        @PathVariable UUID commentId) {
                deleteCommentUseCase.execute(new DeleteCommentCommand(
                                commentId,
                                currentUserId(currentUser)));
        }

        @PostMapping("/{commentId}/pin")
        public ApiResponse<CommentResponse> pin(
                        @AuthenticationPrincipal CurrentUser currentUser,
                        @PathVariable UUID commentId) {
                return ApiResponse.success("Comment pinned", CommentResponse.from(pinCommentUseCase.execute(
                                new PinCommentCommand(
                                                commentId,
                                                currentUserId(currentUser)))));
        }

        @PostMapping("/{commentId}/unpin")
        public ApiResponse<CommentResponse> unpin(
                        @AuthenticationPrincipal CurrentUser currentUser,
                        @PathVariable UUID commentId) {
                return ApiResponse.success("Comment unpinned", CommentResponse.from(unpinCommentUseCase.execute(
                                new UnpinCommentCommand(
                                                commentId,
                                                currentUserId(currentUser)))));
        }

        @PostMapping("/{commentId}/clap")
        public ApiResponse<Void> clap(
                        @AuthenticationPrincipal CurrentUser currentUser,
                        @PathVariable UUID commentId) {
                clapCommentUseCase.execute(new ClapCommentCommand(
                                commentId,
                                currentUserId(currentUser)));
                return ApiResponse.success("Comment clapped", null);
        }

        @DeleteMapping("/{commentId}/clap")
        public ApiResponse<Long> undoClap(
                        @AuthenticationPrincipal CurrentUser currentUser,
                        @PathVariable UUID commentId) {
                long removedCount = undoClapCommentUseCase.execute(new UndoClapCommentCommand(
                                commentId,
                                currentUserId(currentUser)));
                return ApiResponse.success("Comment unclapped", removedCount);
        }

        private UUID currentUserId(CurrentUser currentUser) {
                return UUID.fromString(currentUser.id());
        }
}
