package com.socialmediablog.platform.services.user.infrastructure.persistence;

import com.socialmediablog.platform.services.user.application.port.UserRepository;
import com.socialmediablog.platform.services.user.domain.EmailAddress;
import com.socialmediablog.platform.services.user.domain.User;
import com.socialmediablog.platform.services.user.domain.Username;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaUserRepositoryAdapter implements UserRepository {

    private final SpringDataJpaUserRepository repository;

    public JpaUserRepositoryAdapter(SpringDataJpaUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByUsername(Username username) {
        return repository.existsByUsername(username.value());
    }

    @Override
    public boolean existsByEmail(EmailAddress email) {
        return repository.existsByEmail(email.value());
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(JpaUserEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmailOrUsername(String identifier) {
        String normalized = identifier == null ? "" : identifier.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("@")) {
            return repository.findByEmail(normalized).map(JpaUserEntity::toDomain);
        }
        return repository.findByUsername(normalized).map(JpaUserEntity::toDomain);
    }

    @Override
    public User save(User user) {
        return repository.save(JpaUserEntity.fromDomain(user)).toDomain();
    }
}
