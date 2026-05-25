package com.socialmediablog.platform.services.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.socialmediablog.platform.services.user.application.port.AccessTokenIssuer;
import com.socialmediablog.platform.services.user.application.port.PasswordHasher;
import com.socialmediablog.platform.services.user.application.port.UserRepository;
import com.socialmediablog.platform.services.user.domain.EmailAddress;
import com.socialmediablog.platform.services.user.domain.User;
import com.socialmediablog.platform.services.user.domain.Username;
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
        PasswordHasher passwordHasher = new TestPasswordHasher();
        AccessTokenIssuer accessTokenIssuer = user -> new IssuedToken("token-" + user.username().value(), "Bearer", 3600);
        Clock clock = Clock.fixed(Instant.parse("2026-05-25T00:00:00Z"), ZoneOffset.UTC);
        authApplicationService = new AuthApplicationService(userRepository, passwordHasher, accessTokenIssuer, clock);
    }

    @Test
    void registerCreatesUserAndToken() {
        AuthenticatedUser result = authApplicationService.register(new RegisterUserCommand(
                "Mai.Writer",
                "mai@example.com",
                "password123",
                "Mai Writer"
        ));

        assertThat(result.user().username()).isEqualTo("mai.writer");
        assertThat(result.user().email()).isEqualTo("mai@example.com");
        assertThat(result.token().accessToken()).isEqualTo("token-mai.writer");
        assertThat(userRepository.existsByUsername(Username.of("mai.writer"))).isTrue();
    }

    @Test
    void registerRejectsDuplicateEmail() {
        authApplicationService.register(new RegisterUserCommand(
                "first",
                "same@example.com",
                "password123",
                "First"
        ));

        assertThatThrownBy(() -> authApplicationService.register(new RegisterUserCommand(
                "second",
                "same@example.com",
                "password123",
                "Second"
        ))).isInstanceOf(DuplicateUserException.class);
    }

    @Test
    void loginRejectsInvalidPassword() {
        authApplicationService.register(new RegisterUserCommand(
                "reader",
                "reader@example.com",
                "password123",
                "Reader"
        ));

        assertThatThrownBy(() -> authApplicationService.login(new LoginUserCommand("reader", "wrongpass")))
                .isInstanceOf(InvalidCredentialsException.class);
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
}
