package com.socialmediablog.platform.services.user.domain.aggregate;

import com.socialmediablog.platform.services.user.domain.event.UserRegisteredEvent;
import com.socialmediablog.platform.services.user.domain.model.Role;
import com.socialmediablog.platform.services.user.domain.model.UserStatus;
import com.socialmediablog.platform.services.user.domain.vo.EmailAddress;
import com.socialmediablog.platform.services.user.domain.vo.PasswordHash;
import com.socialmediablog.platform.services.user.domain.vo.Username;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class User {

    private final UUID id;
    private final Username username;
    private final EmailAddress email;
    private final PasswordHash passwordHash;
    private final String displayName;
    private final String bio;
    private final String avatarUrl;
    private final UserStatus status;
    private final Set<Role> roles;
    private final boolean isPrivate;
    private final Instant createdAt;
    private final Instant updatedAt;

    private User(
            UUID id,
            Username username,
            EmailAddress email,
            PasswordHash passwordHash,
            String displayName,
            String bio,
            String avatarUrl,
            UserStatus status,
            Set<Role> roles,
            boolean isPrivate,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = normalizeDisplayName(displayName, username.value());
        this.bio = normalizeBio(bio);
        this.avatarUrl = normalizeAvatarUrl(avatarUrl);
        this.status = status;
        this.roles = Set.copyOf(roles);
        this.isPrivate = isPrivate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User register(
            Username username,
            EmailAddress email,
            PasswordHash passwordHash,
            String displayName,
            Instant now
    ) {
        return new User(
                UUID.randomUUID(),
                username,
                email,
                passwordHash,
                displayName,
                null,
                null,
                UserStatus.ACTIVE,
                new LinkedHashSet<>(Set.of(Role.USER)),
                false,
                now,
                now
        );
    }

    public static User restore(
            UUID id,
            Username username,
            EmailAddress email,
            PasswordHash passwordHash,
            String displayName,
            String bio,
            String avatarUrl,
            UserStatus status,
            Set<Role> roles,
            boolean isPrivate,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new User(id, username, email, passwordHash, displayName, bio, avatarUrl, status, roles, isPrivate, createdAt, updatedAt);
    }

    public UserRegisteredEvent registeredEvent(Instant occurredAt) {
        return new UserRegisteredEvent(UUID.randomUUID(), id, username.value(), email.value(), occurredAt);
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public User updateProfile(String displayName, String bio, String avatarUrl, boolean isPrivate, Instant now) {
        return new User(id, username, email, passwordHash, displayName, bio, avatarUrl, status, roles, isPrivate, createdAt, now);
    }

    public User updateProfile(String displayName, String bio, String avatarUrl, Instant now) {
        return new User(id, username, email, passwordHash, displayName, bio, avatarUrl, status, roles, isPrivate, createdAt, now);
    }

    public User changePassword(PasswordHash passwordHash, Instant now) {
        return new User(id, username, email, passwordHash, displayName, bio, avatarUrl, status, roles, isPrivate, createdAt, now);
    }

    private static String normalizeDisplayName(String displayName, String fallback) {
        String normalized = displayName == null || displayName.isBlank() ? fallback : displayName.trim();
        if (normalized.length() > 80) {
            throw new IllegalArgumentException("Display name must not exceed 80 characters");
        }
        return normalized;
    }

    private static String normalizeBio(String bio) {
        if (bio == null || bio.isBlank()) {
            return null;
        }
        String normalized = bio.trim();
        if (normalized.length() > 500) {
            throw new IllegalArgumentException("Bio must not exceed 500 characters");
        }
        return normalized;
    }

    private static String normalizeAvatarUrl(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return null;
        }
        String normalized = avatarUrl.trim();
        if (normalized.length() > 2048) {
            throw new IllegalArgumentException("Avatar URL must not exceed 2048 characters");
        }
        return normalized;
    }

    public UUID id() {
        return id;
    }

    public Username username() {
        return username;
    }

    public EmailAddress email() {
        return email;
    }

    public PasswordHash passwordHash() {
        return passwordHash;
    }

    public String displayName() {
        return displayName;
    }

    public String bio() {
        return bio;
    }

    public String avatarUrl() {
        return avatarUrl;
    }

    public UserStatus status() {
        return status;
    }

    public Set<Role> roles() {
        return roles;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
