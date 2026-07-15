package com.socialmediablog.platform.services.user.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GoogleLoginRequest(
        @NotBlank
        @Size(max = 8192)
        String credential
) {
}