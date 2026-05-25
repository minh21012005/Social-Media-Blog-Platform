package com.socialmediablog.platform.services.user.api;

import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.user.api.dto.AuthResponse;
import com.socialmediablog.platform.services.user.api.dto.LoginRequest;
import com.socialmediablog.platform.services.user.api.dto.RegisterRequest;
import com.socialmediablog.platform.services.user.application.LoginUserCommand;
import com.socialmediablog.platform.services.user.application.LoginUserUseCase;
import com.socialmediablog.platform.services.user.application.RegisterUserCommand;
import com.socialmediablog.platform.services.user.application.RegisterUserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase, LoginUserUseCase loginUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = AuthResponse.from(registerUserUseCase.register(new RegisterUserCommand(
                request.username(),
                request.email(),
                request.password(),
                request.displayName()
        )));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Registered successfully", response));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = AuthResponse.from(loginUserUseCase.login(new LoginUserCommand(
                request.identifier(),
                request.password()
        )));
        return ApiResponse.success("Logged in successfully", response);
    }
}
