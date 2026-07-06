package com.socialmediablog.platform.services.interaction.api.controller;

import com.socialmediablog.platform.common.security.CurrentUser;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.interaction.api.dto.ServiceStatusResponse;
import com.socialmediablog.platform.services.interaction.application.command.BookmarkArticleCommand;
import com.socialmediablog.platform.services.interaction.application.command.CountArticleLikesQuery;
import com.socialmediablog.platform.services.interaction.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.interaction.application.command.IsArticleLikedQuery;
import com.socialmediablog.platform.services.interaction.application.command.LikeArticleCommand;
import com.socialmediablog.platform.services.interaction.application.command.ListMyBookmarksQuery;
import com.socialmediablog.platform.services.interaction.application.command.RemoveBookmarkCommand;
import com.socialmediablog.platform.services.interaction.application.command.UnlikeArticleCommand;
import com.socialmediablog.platform.services.interaction.application.port.in.BookmarkArticleUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.CountArticleLikesUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.IsArticleLikedUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.LikeArticleUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.ListMyBookmarksUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.RemoveBookmarkUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.UnlikeArticleUseCase;
import java.util.List;
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
    private final ListMyBookmarksUseCase listMyBookmarksUseCase;
    private final LikeArticleUseCase likeArticleUseCase;
    private final UnlikeArticleUseCase unlikeArticleUseCase;
    private final CountArticleLikesUseCase countArticleLikesUseCase;
    private final IsArticleLikedUseCase isArticleLikedUseCase;

    public InteractionController(
            GetServiceStatusUseCase getServiceStatusUseCase,
            BookmarkArticleUseCase bookmarkArticleUseCase,
            RemoveBookmarkUseCase removeBookmarkUseCase,
            ListMyBookmarksUseCase listMyBookmarksUseCase,
            LikeArticleUseCase likeArticleUseCase,
            UnlikeArticleUseCase unlikeArticleUseCase,
            CountArticleLikesUseCase countArticleLikesUseCase,
            IsArticleLikedUseCase isArticleLikedUseCase
    ) {
        this.getServiceStatusUseCase = getServiceStatusUseCase;
        this.bookmarkArticleUseCase = bookmarkArticleUseCase;
        this.removeBookmarkUseCase = removeBookmarkUseCase;
        this.listMyBookmarksUseCase = listMyBookmarksUseCase;
        this.likeArticleUseCase = likeArticleUseCase;
        this.unlikeArticleUseCase = unlikeArticleUseCase;
        this.countArticleLikesUseCase = countArticleLikesUseCase;
        this.isArticleLikedUseCase = isArticleLikedUseCase;
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

    @GetMapping("/bookmarks/me")
    public ApiResponse<List<UUID>> listMyBookmarks(@AuthenticationPrincipal CurrentUser currentUser) {
        return ApiResponse.success(listMyBookmarksUseCase.execute(new ListMyBookmarksQuery(currentUserId(currentUser))));
    }

    @PostMapping("/{articleId}/like")
    public ApiResponse<Void> like(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID articleId
    ) {
        likeArticleUseCase.execute(new LikeArticleCommand(currentUserId(currentUser), articleId));
        return ApiResponse.success("Article liked", null);
    }

    @DeleteMapping("/{articleId}/like")
    public ApiResponse<Void> unlike(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID articleId
    ) {
        unlikeArticleUseCase.execute(new UnlikeArticleCommand(currentUserId(currentUser), articleId));
        return ApiResponse.success("Article unliked", null);
    }

    @GetMapping("/{articleId}/likes")
    public ApiResponse<Long> countLikes(@PathVariable UUID articleId) {
        return ApiResponse.success(countArticleLikesUseCase.execute(new CountArticleLikesQuery(articleId)));
    }

    @GetMapping("/{articleId}/liked")
    public ApiResponse<Boolean> isLiked(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID articleId
    ) {
        return ApiResponse.success(isArticleLikedUseCase.execute(new IsArticleLikedQuery(currentUserId(currentUser), articleId)));
    }

    private UUID currentUserId(CurrentUser currentUser) {
        return UUID.fromString(currentUser.id());
    }
}
