package com.socialmediablog.platform.services.user.api.controller;

import com.socialmediablog.platform.common.security.CurrentUser;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.user.api.dto.ChangePasswordRequest;
import com.socialmediablog.platform.services.user.api.dto.PublicUserProfileResponse;
import com.socialmediablog.platform.services.user.api.dto.UpdateProfileRequest;
import com.socialmediablog.platform.services.user.api.dto.UploadAvatarResponse;
import com.socialmediablog.platform.services.user.api.dto.UserProfileResponse;
import com.socialmediablog.platform.services.user.application.command.ChangePasswordCommand;
import com.socialmediablog.platform.services.user.application.command.UpdateUserProfileCommand;
import com.socialmediablog.platform.services.user.application.command.UploadAvatarCommand;
import com.socialmediablog.platform.services.user.application.port.in.ChangePasswordUseCase;
import com.socialmediablog.platform.services.user.application.port.in.GetCurrentUserUseCase;
import com.socialmediablog.platform.services.user.application.port.in.GetPublicUserByUsernameUseCase;
import com.socialmediablog.platform.services.user.application.port.in.GetPublicUserProfileUseCase;
import com.socialmediablog.platform.services.user.application.port.in.ListPublicUsersUseCase;
import com.socialmediablog.platform.services.user.application.port.in.UpdateCurrentUserUseCase;
import com.socialmediablog.platform.services.user.application.port.in.UploadCurrentUserAvatarUseCase;
import com.socialmediablog.platform.services.user.config.RefreshTokenCookieFactory;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final GetPublicUserProfileUseCase getPublicUserProfileUseCase;
    private final GetPublicUserByUsernameUseCase getPublicUserByUsernameUseCase;
    private final ListPublicUsersUseCase listPublicUsersUseCase;
    private final UpdateCurrentUserUseCase updateCurrentUserUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final UploadCurrentUserAvatarUseCase uploadCurrentUserAvatarUseCase;
    private final RefreshTokenCookieFactory refreshTokenCookies;

    public UserController(
            GetCurrentUserUseCase getCurrentUserUseCase,
            GetPublicUserProfileUseCase getPublicUserProfileUseCase,
            GetPublicUserByUsernameUseCase getPublicUserByUsernameUseCase,
            ListPublicUsersUseCase listPublicUsersUseCase,
            UpdateCurrentUserUseCase updateCurrentUserUseCase,
            ChangePasswordUseCase changePasswordUseCase,
            UploadCurrentUserAvatarUseCase uploadCurrentUserAvatarUseCase,
            RefreshTokenCookieFactory refreshTokenCookies
    ) {
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.getPublicUserProfileUseCase = getPublicUserProfileUseCase;
        this.getPublicUserByUsernameUseCase = getPublicUserByUsernameUseCase;
        this.listPublicUsersUseCase = listPublicUsersUseCase;
        this.updateCurrentUserUseCase = updateCurrentUserUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.uploadCurrentUserAvatarUseCase = uploadCurrentUserAvatarUseCase;
        this.refreshTokenCookies = refreshTokenCookies;
    }

    @GetMapping("/public")
    public ApiResponse<List<PublicUserProfileResponse>> publicProfiles(@RequestParam(name = "ids") List<UUID> ids) {
        return ApiResponse.success(listPublicUsersUseCase.executeBatch(ids).stream()
                .map(PublicUserProfileResponse::from)
                .toList());
    }

    @GetMapping("/search")
    public ApiResponse<List<PublicUserProfileResponse>> searchUsers(@RequestParam(name = "q") String query) {
        return ApiResponse.success(listPublicUsersUseCase.searchUsers(query).stream()
                .map(PublicUserProfileResponse::from)
                .toList());
    }

    @GetMapping("/by-username/{username}")
    public ApiResponse<PublicUserProfileResponse> publicProfileByUsername(@PathVariable String username) {
        return ApiResponse.success(PublicUserProfileResponse.from(getPublicUserByUsernameUseCase.executeByUsername(username)));
    }

    @GetMapping("/{userId}")
    public ApiResponse<PublicUserProfileResponse> publicProfile(@PathVariable UUID userId) {
        return ApiResponse.success(PublicUserProfileResponse.from(getPublicUserProfileUseCase.executePublic(userId)));
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> me(@AuthenticationPrincipal CurrentUser currentUser) {
        UUID userId = currentUserId(currentUser);
        return ApiResponse.success(UserProfileResponse.from(getCurrentUserUseCase.execute(userId)));
    }

    @PatchMapping("/me")
    public ApiResponse<UserProfileResponse> updateMe(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UUID userId = currentUserId(currentUser);
        return ApiResponse.success(
                "Profile updated successfully",
                UserProfileResponse.from(updateCurrentUserUseCase.execute(new UpdateUserProfileCommand(
                        userId,
                        request.displayName(),
                        request.bio(),
                        request.avatarUrl(),
                        request.isPrivate()
                )))
        );
    }

    @PostMapping("/me/avatar")
    public ApiResponse<UploadAvatarResponse> uploadAvatar(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        UUID userId = currentUserId(currentUser);
        return ApiResponse.success(
                "Avatar uploaded successfully",
                UploadAvatarResponse.from(uploadCurrentUserAvatarUseCase.execute(new UploadAvatarCommand(
                        userId,
                        file.getOriginalFilename(),
                        file.getContentType(),
                        file.getBytes()
                )))
        );
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        UUID userId = currentUserId(currentUser);
        changePasswordUseCase.execute(new ChangePasswordCommand(
                userId,
                request.currentPassword(),
                request.newPassword()
        ));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookies.clear().toString())
                .body(ApiResponse.success("Password changed successfully", null));
    }

    private UUID currentUserId(CurrentUser currentUser) {
        return UUID.fromString(currentUser.id());
    }
}
