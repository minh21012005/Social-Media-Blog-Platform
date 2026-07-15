package com.socialmediablog.platform.services.interaction.api.controller;

import com.socialmediablog.platform.common.security.CurrentUser;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.interaction.api.dto.ArticleClapResponse;
import com.socialmediablog.platform.services.interaction.api.dto.ArticleClapStateResponse;
import com.socialmediablog.platform.services.interaction.api.dto.BookmarkResponse;
import com.socialmediablog.platform.services.interaction.api.dto.BookmarkStateResponse;
import com.socialmediablog.platform.services.interaction.api.dto.ServiceStatusResponse;
import com.socialmediablog.platform.services.interaction.application.command.BookmarkArticleCommand;
import com.socialmediablog.platform.services.interaction.application.command.ClapArticleCommand;
import com.socialmediablog.platform.services.interaction.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.interaction.application.command.RemoveBookmarkCommand;
import com.socialmediablog.platform.services.interaction.application.command.UndoClapArticleCommand;
import com.socialmediablog.platform.services.interaction.application.port.in.BookmarkArticleUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.ClapArticleUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.RemoveBookmarkUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.UndoClapArticleUseCase;
import java.util.UUID;
import java.util.List;
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
    private final ClapArticleUseCase clapArticleUseCase;
    private final UndoClapArticleUseCase undoClapArticleUseCase;

    public InteractionController(
            GetServiceStatusUseCase getServiceStatusUseCase,
            BookmarkArticleUseCase bookmarkArticleUseCase,
            RemoveBookmarkUseCase removeBookmarkUseCase,
            ClapArticleUseCase clapArticleUseCase,
            UndoClapArticleUseCase undoClapArticleUseCase) {
        this.getServiceStatusUseCase = getServiceStatusUseCase;
        this.bookmarkArticleUseCase = bookmarkArticleUseCase;
        this.removeBookmarkUseCase = removeBookmarkUseCase;
        this.clapArticleUseCase = clapArticleUseCase;
        this.undoClapArticleUseCase = undoClapArticleUseCase;
    }

    @GetMapping("/status")
    public ApiResponse<ServiceStatusResponse> status(@AuthenticationPrincipal CurrentUser currentUser) {
        String currentUserId = currentUser == null ? "anonymous" : currentUser.id();
        return ApiResponse.success(ServiceStatusResponse.from(getServiceStatusUseCase.execute(
                new GetServiceStatusCommand(currentUserId))));
    }

    @PostMapping("/{articleId}/bookmark")
    public ApiResponse<Void> bookmark(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID articleId) {
        bookmarkArticleUseCase.execute(new BookmarkArticleCommand(currentUserId(currentUser), articleId));
        return ApiResponse.success("Article bookmarked", null);
    }

    @DeleteMapping("/{articleId}/bookmark")
    public ApiResponse<Void> removeBookmark(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID articleId) {
        removeBookmarkUseCase.execute(new RemoveBookmarkCommand(currentUserId(currentUser), articleId));
        return ApiResponse.success("Bookmark removed", null);
    }

    @PostMapping("/{articleId}/clap")
    public ApiResponse<ArticleClapResponse> clap(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID articleId) {
        long clapCount = clapArticleUseCase.execute(new ClapArticleCommand(currentUserId(currentUser), articleId));
        return ApiResponse.success("Article clapped", new ArticleClapResponse(clapCount));
    }

    @DeleteMapping("/{articleId}/clap")
    public ApiResponse<ArticleClapResponse> undoClap(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID articleId) {
        long clapCount = undoClapArticleUseCase
                .execute(new UndoClapArticleCommand(articleId, currentUserId(currentUser)));
        return ApiResponse.success("Article clap undone", new ArticleClapResponse(clapCount));
    }

    @GetMapping("/{articleId}/clap-count")
    public ApiResponse<ArticleClapResponse> clapCount(
            @PathVariable UUID articleId) {
        long clapCount = clapArticleUseCase.getArticleClapCount(articleId);
        return ApiResponse.success("Article clap count", new ArticleClapResponse(clapCount));
    }

    @GetMapping("/{articleId}/clap-state")
    public ApiResponse<ArticleClapStateResponse> clapState(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID articleId) {
        UUID userId = currentUserId(currentUser);
        long clapCount = clapArticleUseCase.getArticleClapCount(articleId);
        boolean clappedByCurrentUser = clapArticleUseCase.hasUserClappedArticle(userId, articleId);
        return ApiResponse.success("Article clap state", new ArticleClapStateResponse(clapCount, clappedByCurrentUser));
    }

    @GetMapping("/bookmarks/me")
    public ApiResponse<List<BookmarkResponse>> myBookmarks(
            @AuthenticationPrincipal CurrentUser currentUser) {
        List<BookmarkResponse> data = bookmarkArticleUseCase.listBookmarks(currentUserId(currentUser)).stream()
                .map(BookmarkResponse::from)
                .toList();
        return ApiResponse.success("Bookmarks loaded", data);
    }

    @GetMapping("/{articleId}/bookmark-state")
    public ApiResponse<BookmarkStateResponse> bookmarkState(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID articleId) {
        boolean bookmarked = bookmarkArticleUseCase.isBookmarked(currentUserId(currentUser), articleId);
        return ApiResponse.success("Bookmark state", new BookmarkStateResponse(bookmarked));
    }

    private UUID currentUserId(CurrentUser currentUser) {
        return UUID.fromString(currentUser.id());
    }
}
