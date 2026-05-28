package com.socialmediablog.platform.services.user.application.usecase;

import com.socialmediablog.platform.services.user.application.command.LoginUserCommand;
import com.socialmediablog.platform.services.user.application.command.LogoutCommand;
import com.socialmediablog.platform.services.user.application.command.RefreshSessionCommand;
import com.socialmediablog.platform.services.user.application.command.RegisterUserCommand;
import com.socialmediablog.platform.services.user.application.command.ChangePasswordCommand;
import com.socialmediablog.platform.services.user.application.command.UpdateUserProfileCommand;
import com.socialmediablog.platform.services.user.application.exception.DuplicateUserException;
import com.socialmediablog.platform.services.user.application.exception.InactiveUserException;
import com.socialmediablog.platform.services.user.application.exception.InvalidCredentialsException;
import com.socialmediablog.platform.services.user.application.exception.InvalidRefreshTokenException;
import com.socialmediablog.platform.services.user.application.exception.UserNotFoundException;
import com.socialmediablog.platform.services.user.application.port.in.ChangePasswordUseCase;
import com.socialmediablog.platform.services.user.application.port.in.GetCurrentUserUseCase;
import com.socialmediablog.platform.services.user.application.port.in.LoginUserUseCase;
import com.socialmediablog.platform.services.user.application.port.in.LogoutUseCase;
import com.socialmediablog.platform.services.user.application.port.in.RefreshSessionUseCase;
import com.socialmediablog.platform.services.user.application.port.in.RegisterUserUseCase;
import com.socialmediablog.platform.services.user.application.port.in.UpdateCurrentUserUseCase;
import com.socialmediablog.platform.services.user.application.port.out.AccessTokenIssuer;
import com.socialmediablog.platform.services.user.application.port.out.DomainEventPublisher;
import com.socialmediablog.platform.services.user.application.port.out.PasswordHasher;
import com.socialmediablog.platform.services.user.application.port.out.RefreshTokenGenerator;
import com.socialmediablog.platform.services.user.application.port.out.RefreshTokenHasher;
import com.socialmediablog.platform.services.user.application.port.out.RefreshTokenRepository;
import com.socialmediablog.platform.services.user.application.result.AuthenticatedUser;
import com.socialmediablog.platform.services.user.application.result.IssuedRefreshToken;
import com.socialmediablog.platform.services.user.application.result.UserProfile;
import com.socialmediablog.platform.services.user.domain.aggregate.RefreshToken;
import com.socialmediablog.platform.services.user.domain.repository.UserRepository;
import com.socialmediablog.platform.services.user.domain.vo.EmailAddress;
import com.socialmediablog.platform.services.user.domain.vo.PasswordHash;
import com.socialmediablog.platform.services.user.domain.vo.PasswordPolicy;
import com.socialmediablog.platform.services.user.domain.aggregate.User;
import com.socialmediablog.platform.services.user.domain.vo.Username;
import com.socialmediablog.platform.common.security.JwtProperties;
import java.time.Instant;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthApplicationService implements
        RegisterUserUseCase,
        LoginUserUseCase,
        RefreshSessionUseCase,
        LogoutUseCase,
        GetCurrentUserUseCase,
        UpdateCurrentUserUseCase,
        ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordHasher passwordHasher;
    private final AccessTokenIssuer accessTokenIssuer;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenHasher refreshTokenHasher;
    private final DomainEventPublisher domainEventPublisher;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public AuthApplicationService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordHasher passwordHasher,
            AccessTokenIssuer accessTokenIssuer,
            RefreshTokenGenerator refreshTokenGenerator,
            RefreshTokenHasher refreshTokenHasher,
            DomainEventPublisher domainEventPublisher,
            JwtProperties jwtProperties,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordHasher = passwordHasher;
        this.accessTokenIssuer = accessTokenIssuer;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.refreshTokenHasher = refreshTokenHasher;
        this.domainEventPublisher = domainEventPublisher;
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    @Override
    @Transactional
    public AuthenticatedUser execute(RegisterUserCommand command) {
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
        domainEventPublisher.publish(savedUser.registeredEvent(clock.instant()));
        return authenticate(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticatedUser execute(LoginUserCommand command) {
        User user = userRepository.findByEmailOrUsername(command.identifier())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username/email or password"));

        if (!passwordHasher.matches(command.password(), user.passwordHash().value())) {
            throw new InvalidCredentialsException("Invalid username/email or password");
        }
        ensureActive(user);
        return authenticate(user);
    }

    @Override
    @Transactional
    public AuthenticatedUser execute(RefreshSessionCommand command) {
        Instant now = clock.instant();
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(refreshTokenHasher.hash(command.refreshToken()))
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token is invalid"));
        if (!refreshToken.isUsableAt(now)) {
            throw new InvalidRefreshTokenException("Refresh token is invalid or expired");
        }

        User user = userRepository.findById(refreshToken.userId())
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token is invalid"));
        ensureActive(user);

        refreshTokenRepository.save(refreshToken.revoke(now));
        return authenticate(user);
    }

    @Override
    @Transactional
    public void execute(LogoutCommand command) {
        String tokenHash = refreshTokenHasher.hash(command.refreshToken());
        refreshTokenRepository.findByTokenHash(tokenHash)
                .filter(refreshToken -> refreshToken.isUsableAt(clock.instant()))
                .filter(refreshToken -> refreshToken.userId().equals(command.userId()))
                .map(refreshToken -> refreshToken.revoke(clock.instant()))
                .ifPresent(refreshTokenRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfile execute(UUID userId) {
        return userRepository.findById(userId)
                .map(UserProfile::from)
                .orElseThrow(() -> new UserNotFoundException("User was not found"));
    }

    @Override
    @Transactional
    public UserProfile execute(UpdateUserProfileCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException("User was not found"));
        ensureActive(user);
        return UserProfile.from(userRepository.save(user.updateProfile(
                command.displayName(),
                command.bio(),
                command.avatarUrl(),
                clock.instant()
        )));
    }

    @Override
    @Transactional
    public void execute(ChangePasswordCommand command) {
        PasswordPolicy.validate(command.newPassword());
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException("User was not found"));
        ensureActive(user);

        if (!passwordHasher.matches(command.currentPassword(), user.passwordHash().value())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        User updatedUser = user.changePassword(
                PasswordHash.of(passwordHasher.hash(command.newPassword())),
                clock.instant()
        );
        userRepository.save(updatedUser);
        refreshTokenRepository.revokeActiveTokensByUserId(user.id(), clock.instant());
    }

    private AuthenticatedUser authenticate(User user) {
        String rawRefreshToken = refreshTokenGenerator.generate();
        Instant now = clock.instant();
        RefreshToken refreshToken = RefreshToken.issue(
                user.id(),
                refreshTokenHasher.hash(rawRefreshToken),
                now.plus(jwtProperties.refreshTokenTtl()),
                now
        );
        refreshTokenRepository.save(refreshToken);
        return new AuthenticatedUser(
                UserProfile.from(user),
                accessTokenIssuer.issue(user),
                new IssuedRefreshToken(rawRefreshToken, jwtProperties.refreshTokenTtl().toSeconds())
        );
    }

    private void ensureActive(User user) {
        if (!user.isActive()) {
            throw new InactiveUserException("User account is not active");
        }
    }
}
