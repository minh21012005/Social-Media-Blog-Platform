package com.socialmediablog.platform.services.user.api.controller;

import com.socialmediablog.platform.common.security.CurrentUser;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.user.api.dto.AuthResponse;
import com.socialmediablog.platform.services.user.api.dto.LoginRequest;
import com.socialmediablog.platform.services.user.api.dto.RegisterRequest;
import com.socialmediablog.platform.services.user.application.command.LoginUserCommand;
import com.socialmediablog.platform.services.user.application.command.LogoutCommand;
import com.socialmediablog.platform.services.user.application.command.RefreshSessionCommand;
import com.socialmediablog.platform.services.user.application.exception.InvalidRefreshTokenException;
import com.socialmediablog.platform.services.user.application.port.in.LoginUserUseCase;
import com.socialmediablog.platform.services.user.application.port.in.LogoutUseCase;
import com.socialmediablog.platform.services.user.application.port.in.RefreshSessionUseCase;
import com.socialmediablog.platform.services.user.application.command.RegisterUserCommand;
import com.socialmediablog.platform.services.user.application.port.in.RegisterUserUseCase;
import com.socialmediablog.platform.services.user.application.result.AuthenticatedUser;
import com.socialmediablog.platform.services.user.config.RefreshTokenCookieProperties;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final RefreshSessionUseCase refreshSessionUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RefreshTokenCookieProperties cookieProperties;

    public AuthController(
            RegisterUserUseCase registerUserUseCase,
            LoginUserUseCase loginUserUseCase,
            RefreshSessionUseCase refreshSessionUseCase,
            LogoutUseCase logoutUseCase,
            RefreshTokenCookieProperties cookieProperties
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.refreshSessionUseCase = refreshSessionUseCase;
        this.logoutUseCase = logoutUseCase;
        this.cookieProperties = cookieProperties;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthenticatedUser authenticatedUser = registerUserUseCase.execute(new RegisterUserCommand(
                request.username(),
                request.email(),
                request.password(),
                request.displayName()
        ));
        return withRefreshCookie(HttpStatus.CREATED, "Registered successfully", authenticatedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthenticatedUser authenticatedUser = loginUserUseCase.execute(new LoginUserCommand(
                request.identifier(),
                request.password()
        ));
        return withRefreshCookie(HttpStatus.OK, "Logged in successfully", authenticatedUser);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = "${security.refresh-cookie.name:refresh_token}", required = false) String refreshToken
    ) {
        AuthenticatedUser authenticatedUser = refreshSessionUseCase.execute(new RefreshSessionCommand(
                requireRefreshToken(refreshToken)
        ));
        return withRefreshCookie(HttpStatus.OK, "Session refreshed successfully", authenticatedUser);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CurrentUser currentUser,
            @CookieValue(name = "${security.refresh-cookie.name:refresh_token}", required = false) String refreshToken
    ) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            logoutUseCase.execute(new LogoutCommand(UUID.fromString(currentUser.id()), refreshToken));
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .body(ApiResponse.success("Logged out successfully", null));
    }

    private ResponseEntity<ApiResponse<AuthResponse>> withRefreshCookie(
            HttpStatus status,
            String message,
            AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, refreshCookie(authenticatedUser).toString())
                .body(ApiResponse.success(message, AuthResponse.from(authenticatedUser)));
    }

    private ResponseCookie refreshCookie(AuthenticatedUser authenticatedUser) {
        return ResponseCookie.from(cookieProperties.name(), authenticatedUser.refreshToken().refreshToken())
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path(cookieProperties.path())
                .maxAge(Duration.ofSeconds(authenticatedUser.refreshToken().expiresInSeconds()))
                .build();
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

    private String requireRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token is missing");
        }
        return refreshToken;
    }
}
