package com.socialmediablog.platform.services.follower.api.controller;

import com.socialmediablog.platform.common.security.CurrentUser;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.follower.api.dto.BlockStatusResponse;
import com.socialmediablog.platform.services.follower.api.dto.FollowCountsResponse;
import com.socialmediablog.platform.services.follower.api.dto.FollowRelationResponse;
import com.socialmediablog.platform.services.follower.api.dto.FollowStatusResponse;
import com.socialmediablog.platform.services.follower.api.dto.FollowUserPageResponse;
import com.socialmediablog.platform.services.follower.api.dto.MutualFollowResponse;
import com.socialmediablog.platform.services.follower.api.dto.ServiceStatusResponse;
import com.socialmediablog.platform.services.follower.application.command.BlockUserCommand;
import com.socialmediablog.platform.services.follower.application.command.CheckMutualFollowCommand;
import com.socialmediablog.platform.services.follower.application.command.FollowUserCommand;
import com.socialmediablog.platform.services.follower.application.command.GetBlockStatusCommand;
import com.socialmediablog.platform.services.follower.application.command.GetFollowCountsCommand;
import com.socialmediablog.platform.services.follower.application.command.GetFollowStatusCommand;
import com.socialmediablog.platform.services.follower.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.follower.application.command.ListBlockedUsersCommand;
import com.socialmediablog.platform.services.follower.application.command.ListFollowersCommand;
import com.socialmediablog.platform.services.follower.application.command.ListFollowingCommand;
import com.socialmediablog.platform.services.follower.application.command.UnblockUserCommand;
import com.socialmediablog.platform.services.follower.application.command.UnfollowUserCommand;
import com.socialmediablog.platform.services.follower.application.port.in.BlockUserUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.CheckMutualFollowUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.FollowUserUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.GetBlockStatusUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.GetFollowCountsUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.GetFollowStatusUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.ListBlockedUsersUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.ListFollowersUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.ListFollowingUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.UnblockUserUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.UnfollowUserUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.AcceptFollowRequestUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.RejectFollowRequestUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.ListPendingFollowRequestsUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.MuteUserUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.UnmuteUserUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.GetMuteStatusUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.ListMutedUsersUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.ListMutedUserIdsUseCase;
import com.socialmediablog.platform.services.follower.application.result.FollowUserPage;
import java.util.List;
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
    private final BlockUserUseCase blockUserUseCase;
    private final UnblockUserUseCase unblockUserUseCase;
    private final GetBlockStatusUseCase getBlockStatusUseCase;
    private final ListBlockedUsersUseCase listBlockedUsersUseCase;
    private final CheckMutualFollowUseCase checkMutualFollowUseCase;
    private final AcceptFollowRequestUseCase acceptFollowRequestUseCase;
    private final RejectFollowRequestUseCase rejectFollowRequestUseCase;
    private final ListPendingFollowRequestsUseCase listPendingFollowRequestsUseCase;
    private final MuteUserUseCase muteUserUseCase;
    private final UnmuteUserUseCase unmuteUserUseCase;
    private final GetMuteStatusUseCase getMuteStatusUseCase;
    private final ListMutedUsersUseCase listMutedUsersUseCase;
    private final ListMutedUserIdsUseCase listMutedUserIdsUseCase;

    public FollowerController(
            GetServiceStatusUseCase getServiceStatusUseCase,
            FollowUserUseCase followUserUseCase,
            UnfollowUserUseCase unfollowUserUseCase,
            GetFollowStatusUseCase getFollowStatusUseCase,
            ListFollowersUseCase listFollowersUseCase,
            ListFollowingUseCase listFollowingUseCase,
            GetFollowCountsUseCase getFollowCountsUseCase,
            BlockUserUseCase blockUserUseCase,
            UnblockUserUseCase unblockUserUseCase,
            GetBlockStatusUseCase getBlockStatusUseCase,
            ListBlockedUsersUseCase listBlockedUsersUseCase,
            CheckMutualFollowUseCase checkMutualFollowUseCase,
            AcceptFollowRequestUseCase acceptFollowRequestUseCase,
            RejectFollowRequestUseCase rejectFollowRequestUseCase,
            ListPendingFollowRequestsUseCase listPendingFollowRequestsUseCase,
            MuteUserUseCase muteUserUseCase,
            UnmuteUserUseCase unmuteUserUseCase,
            GetMuteStatusUseCase getMuteStatusUseCase,
            ListMutedUsersUseCase listMutedUsersUseCase,
            ListMutedUserIdsUseCase listMutedUserIdsUseCase
    ) {
        this.getServiceStatusUseCase = getServiceStatusUseCase;
        this.followUserUseCase = followUserUseCase;
        this.unfollowUserUseCase = unfollowUserUseCase;
        this.getFollowStatusUseCase = getFollowStatusUseCase;
        this.listFollowersUseCase = listFollowersUseCase;
        this.listFollowingUseCase = listFollowingUseCase;
        this.getFollowCountsUseCase = getFollowCountsUseCase;
        this.blockUserUseCase = blockUserUseCase;
        this.unblockUserUseCase = unblockUserUseCase;
        this.getBlockStatusUseCase = getBlockStatusUseCase;
        this.listBlockedUsersUseCase = listBlockedUsersUseCase;
        this.checkMutualFollowUseCase = checkMutualFollowUseCase;
        this.acceptFollowRequestUseCase = acceptFollowRequestUseCase;
        this.rejectFollowRequestUseCase = rejectFollowRequestUseCase;
        this.listPendingFollowRequestsUseCase = listPendingFollowRequestsUseCase;
        this.muteUserUseCase = muteUserUseCase;
        this.unmuteUserUseCase = unmuteUserUseCase;
        this.getMuteStatusUseCase = getMuteStatusUseCase;
        this.listMutedUsersUseCase = listMutedUsersUseCase;
        this.listMutedUserIdsUseCase = listMutedUserIdsUseCase;
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

    @PostMapping("/{userId}/block")
    public ApiResponse<FollowRelationResponse> block(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID userId
    ) {
        return ApiResponse.success(FollowRelationResponse.from(blockUserUseCase.execute(
                new BlockUserCommand(currentUserId(currentUser), userId)
        )));
    }

    @DeleteMapping("/{userId}/block")
    public ApiResponse<FollowRelationResponse> unblock(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID userId
    ) {
        return ApiResponse.success(FollowRelationResponse.from(unblockUserUseCase.execute(
                new UnblockUserCommand(currentUserId(currentUser), userId)
        )));
    }

    @GetMapping("/{userId}/block-status")
    public ApiResponse<BlockStatusResponse> blockStatus(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID userId
    ) {
        return ApiResponse.success(BlockStatusResponse.from(getBlockStatusUseCase.execute(
                new GetBlockStatusCommand(currentUserId(currentUser), userId)
        )));
    }

    @GetMapping("/me/blocked")
    public ApiResponse<FollowUserPageResponse> blockedUsers(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(FollowUserPageResponse.from(listBlockedUsersUseCase.execute(
                new ListBlockedUsersCommand(currentUserId(currentUser), page, size)
        )));
    }

    @GetMapping("/{userId}/mutual")
    public ApiResponse<MutualFollowResponse> mutual(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID userId
    ) {
        return ApiResponse.success(MutualFollowResponse.from(checkMutualFollowUseCase.execute(
                new CheckMutualFollowCommand(currentUserId(currentUser), userId)
        )));
    }

    @GetMapping("/requests/pending")
    public ApiResponse<FollowUserPageResponse> pendingRequests(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(FollowUserPageResponse.from(listPendingFollowRequestsUseCase.listPending(
                currentUserId(currentUser), page, size
        )));
    }

    @PostMapping("/requests/{followerId}/accept")
    public ApiResponse<FollowRelationResponse> acceptRequest(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID followerId
    ) {
        return ApiResponse.success(FollowRelationResponse.from(acceptFollowRequestUseCase.accept(
                currentUserId(currentUser), followerId
        )));
    }

    @DeleteMapping("/requests/{followerId}/reject")
    public ApiResponse<FollowRelationResponse> rejectRequest(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID followerId
    ) {
        return ApiResponse.success(FollowRelationResponse.from(rejectFollowRequestUseCase.reject(
                currentUserId(currentUser), followerId
        )));
    }

    @PostMapping("/{userId}/mute")
    public ApiResponse<Void> mute(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID userId
    ) {
        muteUserUseCase.mute(currentUserId(currentUser), userId);
        return ApiResponse.success("User muted successfully", null);
    }

    @DeleteMapping("/{userId}/mute")
    public ApiResponse<Void> unmute(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID userId
    ) {
        unmuteUserUseCase.unmute(currentUserId(currentUser), userId);
        return ApiResponse.success("User unmuted successfully", null);
    }

    @GetMapping("/{userId}/mute-status")
    public ApiResponse<Boolean> muteStatus(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID userId
    ) {
        return ApiResponse.success(getMuteStatusUseCase.isMuted(currentUserId(currentUser), userId));
    }

    @GetMapping("/me/muted")
    public ApiResponse<FollowUserPageResponse> mutedUsers(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(FollowUserPageResponse.from(listMutedUsersUseCase.listMuted(
                currentUserId(currentUser), page, size
        )));
    }

    @GetMapping("/me/muted-ids")
    public ApiResponse<List<UUID>> mutedUserIds(@AuthenticationPrincipal CurrentUser currentUser) {
        return ApiResponse.success(listMutedUserIdsUseCase.listMutedIds(currentUserId(currentUser)));
    }

    private static UUID currentUserId(CurrentUser currentUser) {
        return UUID.fromString(currentUser.id());
    }
}
