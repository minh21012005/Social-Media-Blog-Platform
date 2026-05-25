package com.socialmediablog.platform.services.user.application;

import com.socialmediablog.platform.services.user.application.port.AccessTokenIssuer;
import com.socialmediablog.platform.services.user.application.port.PasswordHasher;
import com.socialmediablog.platform.services.user.application.port.UserRepository;
import com.socialmediablog.platform.services.user.domain.EmailAddress;
import com.socialmediablog.platform.services.user.domain.PasswordHash;
import com.socialmediablog.platform.services.user.domain.PasswordPolicy;
import com.socialmediablog.platform.services.user.domain.User;
import com.socialmediablog.platform.services.user.domain.Username;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthApplicationService implements RegisterUserUseCase, LoginUserUseCase, GetCurrentUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final AccessTokenIssuer accessTokenIssuer;
    private final Clock clock;

    public AuthApplicationService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            AccessTokenIssuer accessTokenIssuer,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.accessTokenIssuer = accessTokenIssuer;
        this.clock = clock;
    }

    @Override
    @Transactional
    public AuthenticatedUser register(RegisterUserCommand command) {
        PasswordPolicy.validate(command.password());
        Username username = Username.of(command.username());
        EmailAddress email = EmailAddress.of(command.email());

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUserException("Username is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException("Email is already registered");
        }

        User user = User.register(
                username,
                email,
                PasswordHash.of(passwordHasher.hash(command.password())),
                command.displayName(),
                clock.instant()
        );
        User savedUser = userRepository.save(user);
        savedUser.registeredEvent(clock.instant());
        return authenticate(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticatedUser login(LoginUserCommand command) {
        User user = userRepository.findByEmailOrUsername(command.identifier())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username/email or password"));

        if (!passwordHasher.matches(command.password(), user.passwordHash().value())) {
            throw new InvalidCredentialsException("Invalid username/email or password");
        }
        return authenticate(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfile getCurrentUser(UUID userId) {
        return userRepository.findById(userId)
                .map(UserProfile::from)
                .orElseThrow(() -> new UserNotFoundException("User was not found"));
    }

    private AuthenticatedUser authenticate(User user) {
        return new AuthenticatedUser(UserProfile.from(user), accessTokenIssuer.issue(user));
    }
}
