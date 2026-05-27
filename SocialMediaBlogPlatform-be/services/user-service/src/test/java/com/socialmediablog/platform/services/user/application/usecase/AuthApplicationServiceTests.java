package com.socialmediablog.platform.services.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.socialmediablog.platform.common.events.DomainEvent;
import com.socialmediablog.platform.common.security.JwtProperties;
import com.socialmediablog.platform.services.user.application.command.LoginUserCommand;
import com.socialmediablog.platform.services.user.application.command.LogoutCommand;
import com.socialmediablog.platform.services.user.application.command.RefreshSessionCommand;
import com.socialmediablog.platform.services.user.application.command.RegisterUserCommand;
import com.socialmediablog.platform.services.user.application.exception.DuplicateUserException;
import com.socialmediablog.platform.services.user.application.exception.InvalidCredentialsException;
import com.socialmediablog.platform.services.user.application.exception.InvalidRefreshTokenException;
import com.socialmediablog.platform.services.user.application.port.out.AccessTokenIssuer;
import com.socialmediablog.platform.services.user.application.port.out.DomainEventPublisher;
import com.socialmediablog.platform.services.user.application.port.out.PasswordHasher;
import com.socialmediablog.platform.services.user.application.port.out.RefreshTokenGenerator;
import com.socialmediablog.platform.services.user.application.port.out.RefreshTokenHasher;
import com.socialmediablog.platform.services.user.application.port.out.RefreshTokenRepository;
import com.socialmediablog.platform.services.user.application.result.AuthenticatedUser;
import com.socialmediablog.platform.services.user.application.result.IssuedToken;
import com.socialmediablog.platform.services.user.domain.aggregate.RefreshToken;
import com.socialmediablog.platform.services.user.domain.repository.UserRepository;
import com.socialmediablog.platform.services.user.domain.vo.EmailAddress;
import com.socialmediablog.platform.services.user.domain.aggregate.User;
import com.socialmediablog.platform.services.user.domain.vo.Username;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthApplicationServiceTests {

    private InMemoryUserRepository userRepository;
    private AuthApplicationService authApplicationService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        InMemoryRefreshTokenRepository refreshTokenRepository = new InMemoryRefreshTokenRepository();
        PasswordHasher passwordHasher = new TestPasswordHasher();
        AccessTokenIssuer accessTokenIssuer = user -> new IssuedToken("token-" + user.username().value(), "Bearer", 3600);
        RefreshTokenGenerator refreshTokenGenerator = new TestRefreshTokenGenerator();
        RefreshTokenHasher refreshTokenHasher = refreshToken -> "hash:" + refreshToken;
        DomainEventPublisher domainEventPublisher = event -> {
        };
        JwtProperties jwtProperties = new JwtProperties();
        Clock clock = Clock.fixed(Instant.parse("2026-05-25T00:00:00Z"), ZoneOffset.UTC);
        authApplicationService = new AuthApplicationService(
                userRepository,
                refreshTokenRepository,
                passwordHasher,
                accessTokenIssuer,
                refreshTokenGenerator,
                refreshTokenHasher,
                domainEventPublisher,
                jwtProperties,
                clock
        );
    }

    @Test
    void registerCreatesUserAndToken() {
        AuthenticatedUser result = authApplicationService.execute(new RegisterUserCommand(
                "Mai.Writer",
                "mai@example.com",
                "password123",
                "Mai Writer"
        ));

        assertThat(result.user().username()).isEqualTo("mai.writer");
        assertThat(result.user().email()).isEqualTo("mai@example.com");
        assertThat(result.token().accessToken()).isEqualTo("token-mai.writer");
        assertThat(result.refreshToken().refreshToken()).startsWith("refresh-token-");
        assertThat(userRepository.existsByUsername(Username.of("mai.writer"))).isTrue();
    }

    @Test
    void registerRejectsDuplicateEmail() {
        authApplicationService.execute(new RegisterUserCommand(
                "first",
                "same@example.com",
                "password123",
                "First"
        ));

        assertThatThrownBy(() -> authApplicationService.execute(new RegisterUserCommand(
                "second",
                "same@example.com",
                "password123",
                "Second"
        ))).isInstanceOf(DuplicateUserException.class);
    }

    @Test
    void loginRejectsInvalidPassword() {
        authApplicationService.execute(new RegisterUserCommand(
                "reader",
                "reader@example.com",
                "password123",
                "Reader"
        ));

        assertThatThrownBy(() -> authApplicationService.execute(new LoginUserCommand("reader", "wrongpass")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refreshRotatesRefreshToken() {
        AuthenticatedUser registered = authApplicationService.execute(new RegisterUserCommand(
                "reader",
                "reader@example.com",
                "password123",
                "Reader"
        ));

        AuthenticatedUser refreshed = authApplicationService.execute(new RefreshSessionCommand(
                registered.refreshToken().refreshToken()
        ));

        assertThat(refreshed.token().accessToken()).isEqualTo("token-reader");
        assertThat(refreshed.refreshToken().refreshToken()).isNotEqualTo(registered.refreshToken().refreshToken());
        assertThatThrownBy(() -> authApplicationService.execute(new RefreshSessionCommand(
                registered.refreshToken().refreshToken()
        ))).isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void logoutRevokesRefreshToken() {
        AuthenticatedUser registered = authApplicationService.execute(new RegisterUserCommand(
                "reader",
                "reader@example.com",
                "password123",
                "Reader"
        ));

        authApplicationService.execute(new LogoutCommand(registered.user().id(), registered.refreshToken().refreshToken()));

        assertThatThrownBy(() -> authApplicationService.execute(new RefreshSessionCommand(
                registered.refreshToken().refreshToken()
        ))).isInstanceOf(InvalidRefreshTokenException.class);
    }

    private static class InMemoryUserRepository implements UserRepository {

        private final Map<UUID, User> users = new HashMap<>();

        @Override
        public boolean existsByUsername(Username username) {
            return users.values().stream().anyMatch(user -> user.username().equals(username));
        }

        @Override
        public boolean existsByEmail(EmailAddress email) {
            return users.values().stream().anyMatch(user -> user.email().equals(email));
        }

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.ofNullable(users.get(id));
        }

        @Override
        public Optional<User> findByEmailOrUsername(String identifier) {
            String normalized = identifier.trim().toLowerCase(Locale.ROOT);
            return users.values().stream()
                    .filter(user -> user.email().value().equals(normalized) || user.username().value().equals(normalized))
                    .findFirst();
        }

        @Override
        public User save(User user) {
            users.put(user.id(), user);
            return user;
        }
    }

    private static class TestPasswordHasher implements PasswordHasher {

        @Override
        public String hash(String rawPassword) {
            return "hashed:" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String hashedPassword) {
            return hash(rawPassword).equals(hashedPassword);
        }
    }

    private static class InMemoryRefreshTokenRepository implements RefreshTokenRepository {

        private final Map<String, RefreshToken> refreshTokens = new HashMap<>();

        @Override
        public Optional<RefreshToken> findByTokenHash(String tokenHash) {
            return Optional.ofNullable(refreshTokens.get(tokenHash));
        }

        @Override
        public RefreshToken save(RefreshToken refreshToken) {
            refreshTokens.put(refreshToken.tokenHash(), refreshToken);
            return refreshToken;
        }

        @Override
        public void revokeActiveTokensByUserId(UUID userId, Instant revokedAt) {
            refreshTokens.replaceAll((key, refreshToken) -> refreshToken.userId().equals(userId)
                    ? refreshToken.revoke(revokedAt)
                    : refreshToken);
        }
    }

    private static class TestRefreshTokenGenerator implements RefreshTokenGenerator {

        private int current = 1;

        @Override
        public String generate() {
            return "refresh-token-" + current++;
        }
    }
}
