package com.socialmediablog.platform.services.interaction.api.controller;

import com.socialmediablog.platform.common.security.CurrentUser;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.interaction.api.dto.ServiceStatusResponse;
import com.socialmediablog.platform.services.interaction.application.command.BookmarkArticleCommand;
import com.socialmediablog.platform.services.interaction.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.interaction.application.command.RemoveBookmarkCommand;
import com.socialmediablog.platform.services.interaction.application.port.in.BookmarkArticleUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.RemoveBookmarkUseCase;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interactions")
public class InteractionController {

    private final GetServiceStatusUseCase getServiceStatusUseCase;
    private final BookmarkArticleUseCase bookmarkArticleUseCase;
    private final RemoveBookmarkUseCase removeBookmarkUseCase;

    public InteractionController(
            GetServiceStatusUseCase getServiceStatusUseCase,
            BookmarkArticleUseCase bookmarkArticleUseCase,
            RemoveBookmarkUseCase removeBookmarkUseCase
    ) {
        this.getServiceStatusUseCase = getServiceStatusUseCase;
        this.bookmarkArticleUseCase = bookmarkArticleUseCase;
        this.removeBookmarkUseCase = removeBookmarkUseCase;
    }

    @GetMapping("/status")
    public ApiResponse<ServiceStatusResponse> status(@AuthenticationPrincipal CurrentUser currentUser) {
        String currentUserId = currentUser == null ? "anonymous" : currentUser.id();
        return ApiResponse.success(ServiceStatusResponse.from(getServiceStatusUseCase.execute(
                new GetServiceStatusCommand(currentUserId)
        )));
    }

    @PostMapping("/{articleId}/bookmark")
    public ApiResponse<Void> bookmark(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID articleId
    ) {
        bookmarkArticleUseCase.execute(new BookmarkArticleCommand(currentUserId(currentUser), articleId));
        return ApiResponse.success("Article bookmarked", null);
    }

    @DeleteMapping("/{articleId}/bookmark")
    public ApiResponse<Void> removeBookmark(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID articleId
    ) {
        removeBookmarkUseCase.execute(new RemoveBookmarkCommand(currentUserId(currentUser), articleId));
        return ApiResponse.success("Bookmark removed", null);
    }

    private UUID currentUserId(CurrentUser currentUser) {
        return UUID.fromString(currentUser.id());
    }
}
