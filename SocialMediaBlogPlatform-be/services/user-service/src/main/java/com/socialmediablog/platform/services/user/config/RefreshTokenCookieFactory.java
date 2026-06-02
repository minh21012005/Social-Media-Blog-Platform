package com.socialmediablog.platform.services.user.config;

import com.socialmediablog.platform.services.user.application.result.AuthenticatedUser;
import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieFactory {

    private final RefreshTokenCookieProperties properties;

    public RefreshTokenCookieFactory(RefreshTokenCookieProperties properties) {
        this.properties = properties;
    }

    public ResponseCookie issue(AuthenticatedUser authenticatedUser) {
        return baseCookie(authenticatedUser.refreshToken().refreshToken())
                .maxAge(Duration.ofSeconds(authenticatedUser.refreshToken().expiresInSeconds()))
                .build();
    }

    public ResponseCookie clear() {
        return baseCookie("")
                .maxAge(Duration.ZERO)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(properties.getName(), value)
                .httpOnly(true)
                .secure(properties.isSecure())
                .sameSite(properties.getSameSite())
                .path(properties.getPath());
    }
}
