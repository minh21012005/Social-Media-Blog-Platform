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
    private final UserStatus status;
    private final Set<Role> roles;
    private final Instant createdAt;
    private final Instant updatedAt;

    private User(
            UUID id,
            Username username,
            EmailAddress email,
            PasswordHash passwordHash,
            String displayName,
            UserStatus status,
            Set<Role> roles,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = normalizeDisplayName(displayName, username.value());
        this.status = status;
        this.roles = Set.copyOf(roles);
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
                UserStatus.ACTIVE,
                new LinkedHashSet<>(Set.of(Role.USER)),
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
            UserStatus status,
            Set<Role> roles,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new User(id, username, email, passwordHash, displayName, status, roles, createdAt, updatedAt);
    }

    public UserRegisteredEvent registeredEvent(Instant occurredAt) {
        return new UserRegisteredEvent(UUID.randomUUID(), id, username.value(), email.value(), occurredAt);
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public User updateProfile(String displayName, Instant now) {
        return new User(id, username, email, passwordHash, displayName, status, roles, createdAt, now);
    }

    public User changePassword(PasswordHash passwordHash, Instant now) {
        return new User(id, username, email, passwordHash, displayName, status, roles, createdAt, now);
    }

    private static String normalizeDisplayName(String displayName, String fallback) {
        String normalized = displayName == null || displayName.isBlank() ? fallback : displayName.trim();
        if (normalized.length() > 80) {
            throw new IllegalArgumentException("Display name must not exceed 80 characters");
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

    public UserStatus status() {
        return status;
    }

    public Set<Role> roles() {
        return roles;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
