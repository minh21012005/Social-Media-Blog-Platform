package com.socialmediablog.platform.services.follower.api.controller;

import com.socialmediablog.platform.common.security.CurrentUser;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.follower.api.dto.FollowCountsResponse;
import com.socialmediablog.platform.services.follower.api.dto.FollowRelationResponse;
import com.socialmediablog.platform.services.follower.api.dto.FollowStatusResponse;
import com.socialmediablog.platform.services.follower.api.dto.FollowUserPageResponse;
import com.socialmediablog.platform.services.follower.api.dto.ServiceStatusResponse;
import com.socialmediablog.platform.services.follower.application.command.FollowUserCommand;
import com.socialmediablog.platform.services.follower.application.command.GetFollowCountsCommand;
import com.socialmediablog.platform.services.follower.application.command.GetFollowStatusCommand;
import com.socialmediablog.platform.services.follower.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.follower.application.command.ListFollowersCommand;
import com.socialmediablog.platform.services.follower.application.command.ListFollowingCommand;
import com.socialmediablog.platform.services.follower.application.command.UnfollowUserCommand;
import com.socialmediablog.platform.services.follower.application.port.in.FollowUserUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.GetFollowCountsUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.GetFollowStatusUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.ListFollowersUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.ListFollowingUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.UnfollowUserUseCase;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/follows")
public class FollowerController {

    private final GetServiceStatusUseCase getServiceStatusUseCase;
    private final FollowUserUseCase followUserUseCase;
    private final UnfollowUserUseCase unfollowUserUseCase;
    private final GetFollowStatusUseCase getFollowStatusUseCase;
    private final ListFollowersUseCase listFollowersUseCase;
    private final ListFollowingUseCase listFollowingUseCase;
    private final GetFollowCountsUseCase getFollowCountsUseCase;

    public FollowerController(
            GetServiceStatusUseCase getServiceStatusUseCase,
            FollowUserUseCase followUserUseCase,
            UnfollowUserUseCase unfollowUserUseCase,
            GetFollowStatusUseCase getFollowStatusUseCase,
            ListFollowersUseCase listFollowersUseCase,
            ListFollowingUseCase listFollowingUseCase,
            GetFollowCountsUseCase getFollowCountsUseCase
    ) {
        this.getServiceStatusUseCase = getServiceStatusUseCase;
        this.followUserUseCase = followUserUseCase;
        this.unfollowUserUseCase = unfollowUserUseCase;
        this.getFollowStatusUseCase = getFollowStatusUseCase;
        this.listFollowersUseCase = listFollowersUseCase;
        this.listFollowingUseCase = listFollowingUseCase;
        this.getFollowCountsUseCase = getFollowCountsUseCase;
    }

    @GetMapping("/status")
    public ApiResponse<ServiceStatusResponse> status(@AuthenticationPrincipal CurrentUser currentUser) {
        return ApiResponse.success(ServiceStatusResponse.from(getServiceStatusUseCase.execute(
                new GetServiceStatusCommand(currentUser.id())
        )));
    }

    @PostMapping("/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FollowRelationResponse> follow(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID userId
    ) {
        return ApiResponse.success(FollowRelationResponse.from(followUserUseCase.execute(
                new FollowUserCommand(currentUserId(currentUser), userId)
        )));
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<FollowRelationResponse> unfollow(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID userId
    ) {
        return ApiResponse.success(FollowRelationResponse.from(unfollowUserUseCase.execute(
                new UnfollowUserCommand(currentUserId(currentUser), userId)
        )));
    }

    @GetMapping("/{userId}/status")
    public ApiResponse<FollowStatusResponse> followStatus(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID userId
    ) {
        return ApiResponse.success(FollowStatusResponse.from(getFollowStatusUseCase.execute(
                new GetFollowStatusCommand(currentUserId(currentUser), userId)
        )));
    }

    @GetMapping("/{userId}/followers")
    public ApiResponse<FollowUserPageResponse> followers(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(FollowUserPageResponse.from(listFollowersUseCase.execute(
                new ListFollowersCommand(userId, page, size)
        )));
    }

    @GetMapping("/{userId}/following")
    public ApiResponse<FollowUserPageResponse> following(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(FollowUserPageResponse.from(listFollowingUseCase.execute(
                new ListFollowingCommand(userId, page, size)
        )));
    }

    @GetMapping("/{userId}/counts")
    public ApiResponse<FollowCountsResponse> counts(@PathVariable UUID userId) {
        return ApiResponse.success(FollowCountsResponse.from(getFollowCountsUseCase.execute(
                new GetFollowCountsCommand(userId)
        )));
    }

    private static UUID currentUserId(CurrentUser currentUser) {
        return UUID.fromString(currentUser.id());
    }
}
