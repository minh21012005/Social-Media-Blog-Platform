package com.socialmediablog.platform.services.user.infrastructure.security;

import com.socialmediablog.platform.services.user.application.exception.InvalidCredentialsException;
import com.socialmediablog.platform.services.user.application.port.out.GoogleIdentityVerifier;
import com.socialmediablog.platform.services.user.application.result.VerifiedGoogleIdentity;
import com.socialmediablog.platform.services.user.config.GoogleOAuthProperties;
import java.util.List;
import java.util.Set;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class NimbusGoogleIdentityVerifier implements GoogleIdentityVerifier {

    private static final Set<String> ALLOWED_ISSUERS = Set.of(
            "https://accounts.google.com",
            "accounts.google.com"
    );

    private final GoogleOAuthProperties properties;
    private final JwtDecoder jwtDecoder;

    public NimbusGoogleIdentityVerifier(GoogleOAuthProperties properties) {
        this.properties = properties;
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(properties.getJwkSetUri()).build();
        OAuth2TokenValidator<Jwt> googleClaimsValidator = this::validateGoogleClaims;
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                googleClaimsValidator
        ));
        this.jwtDecoder = decoder;
    }

    @Override
    public VerifiedGoogleIdentity verify(String credential) {
        if (properties.getClientId() == null
                || properties.getClientId().isBlank()
                || "not-configured".equals(properties.getClientId())) {
            throw new IllegalStateException("Google login is not configured");
        }

        Jwt jwt = jwtDecoder.decode(credential);
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank() || !isEmailVerified(jwt.getClaim("email_verified"))) {
            throw new InvalidCredentialsException("Google account email must be verified");
        }

        String displayName = jwt.getClaimAsString("name");
        if (displayName == null || displayName.isBlank()) {
            displayName = email.substring(0, email.indexOf('@'));
        }
        return new VerifiedGoogleIdentity(jwt.getSubject(), email, displayName);
    }

    private OAuth2TokenValidatorResult validateGoogleClaims(Jwt jwt) {
        List<String> audience = jwt.getAudience();
        boolean validIssuer = jwt.getIssuer() != null && ALLOWED_ISSUERS.contains(jwt.getIssuer().toString());
        boolean validAudience = audience != null && audience.contains(properties.getClientId());
        if (validIssuer && validAudience) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                "invalid_token",
                "Google ID token has an invalid issuer or audience",
                null
        ));
    }

    private boolean isEmailVerified(Object claim) {
        return Boolean.TRUE.equals(claim) || "true".equalsIgnoreCase(String.valueOf(claim));
    }
}