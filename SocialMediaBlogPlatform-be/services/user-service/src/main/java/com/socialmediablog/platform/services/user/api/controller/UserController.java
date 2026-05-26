package com.socialmediablog.platform.services.user.api.controller;

import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.user.api.dto.UserProfileResponse;
import com.socialmediablog.platform.services.user.application.port.in.GetCurrentUserUseCase;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final GetCurrentUserUseCase getCurrentUserUseCase;

    public UserController(GetCurrentUserUseCase getCurrentUserUseCase) {
        this.getCurrentUserUseCase = getCurrentUserUseCase;
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> me(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(UserProfileResponse.from(getCurrentUserUseCase.execute(userId)));
    }
}
