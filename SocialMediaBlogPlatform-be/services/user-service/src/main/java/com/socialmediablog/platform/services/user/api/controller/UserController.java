package com.socialmediablog.platform.services.user.api.controller;

import com.socialmediablog.platform.common.security.CurrentUser;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.user.api.dto.ChangePasswordRequest;
import com.socialmediablog.platform.services.user.api.dto.UpdateProfileRequest;
import com.socialmediablog.platform.services.user.api.dto.UserProfileResponse;
import com.socialmediablog.platform.services.user.application.command.ChangePasswordCommand;
import com.socialmediablog.platform.services.user.application.command.UpdateUserProfileCommand;
import com.socialmediablog.platform.services.user.application.port.in.ChangePasswordUseCase;
import com.socialmediablog.platform.services.user.application.port.in.GetCurrentUserUseCase;
import com.socialmediablog.platform.services.user.application.port.in.UpdateCurrentUserUseCase;
import com.socialmediablog.platform.services.user.config.RefreshTokenCookieProperties;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final UpdateCurrentUserUseCase updateCurrentUserUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final RefreshTokenCookieProperties cookieProperties;

    public UserController(
            GetCurrentUserUseCase getCurrentUserUseCase,
            UpdateCurrentUserUseCase updateCurrentUserUseCase,
            ChangePasswordUseCase changePasswordUseCase,
            RefreshTokenCookieProperties cookieProperties
    ) {
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.updateCurrentUserUseCase = updateCurrentUserUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.cookieProperties = cookieProperties;
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
                        request.displayName()
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
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .body(ApiResponse.success("Password changed successfully", null));
    }

    private UUID currentUserId(CurrentUser currentUser) {
        return UUID.fromString(currentUser.id());
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(cookieProperties.name(), "")
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path(cookieProperties.path())
                .maxAge(Duration.ZERO)
                .build();
    }
}
