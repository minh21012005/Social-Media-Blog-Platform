package com.socialmediablog.platform.services.user.infrastructure.entity;

import com.socialmediablog.platform.common.web.entity.BaseEntity;
import com.socialmediablog.platform.services.user.domain.vo.EmailAddress;
import com.socialmediablog.platform.services.user.domain.vo.PasswordHash;
import com.socialmediablog.platform.services.user.domain.model.Role;
import com.socialmediablog.platform.services.user.domain.aggregate.User;
import com.socialmediablog.platform.services.user.domain.model.UserStatus;
import com.socialmediablog.platform.services.user.domain.vo.Username;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "app_users")
public class JpaUserEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 80)
    private String displayName;

    @Column(length = 500)
    private String bio;

    @Column(name = "avatar_url", length = 2048)
    private String avatarUrl;

    @Column(nullable = false, length = 20)
    private String status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false, length = 30)
    private Set<String> roles = new LinkedHashSet<>();

    protected JpaUserEntity() {
    }

    private JpaUserEntity(
            UUID id,
            String username,
            String email,
            String passwordHash,
            String displayName,
            String bio,
            String avatarUrl,
            String status,
            Set<String> roles,
            java.time.Instant createdAt,
            java.time.Instant updatedAt
    ) {
        super(id, createdAt, updatedAt);
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.status = status;
        this.roles = new LinkedHashSet<>(roles);
    }

    public static JpaUserEntity fromDomain(User user) {
        return new JpaUserEntity(
                user.id(),
                user.username().value(),
                user.email().value(),
                user.passwordHash().value(),
                user.displayName(),
                user.bio(),
                user.avatarUrl(),
                user.status().name(),
                user.roles().stream().map(Role::name).collect(Collectors.toCollection(LinkedHashSet::new)),
                user.createdAt(),
                user.updatedAt()
        );
    }

    public User toDomain() {
        return User.restore(
                id,
                Username.of(username),
                EmailAddress.of(email),
                PasswordHash.of(passwordHash),
                displayName,
                bio,
                avatarUrl,
                UserStatus.valueOf(status),
                roles.stream().map(Role::valueOf).collect(Collectors.toCollection(LinkedHashSet::new)),
                createdAt,
                updatedAt
        );
    }
}
