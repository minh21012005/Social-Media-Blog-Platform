package com.socialmediablog.platform.services.user.application.result;

public record VerifiedGoogleIdentity(
        String subject,
        String email,
        String displayName
) {
}