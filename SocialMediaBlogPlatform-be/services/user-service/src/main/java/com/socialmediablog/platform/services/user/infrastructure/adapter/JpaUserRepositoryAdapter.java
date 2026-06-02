package com.socialmediablog.platform.services.user.infrastructure.adapter;

import com.socialmediablog.platform.services.user.domain.repository.UserRepository;
import com.socialmediablog.platform.services.user.domain.vo.EmailAddress;
import com.socialmediablog.platform.services.user.domain.aggregate.User;
import com.socialmediablog.platform.services.user.domain.vo.Username;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import com.socialmediablog.platform.services.user.infrastructure.entity.JpaUserEntity;
import com.socialmediablog.platform.services.user.infrastructure.persistence.SpringDataJpaUserRepository;
import java.util.List;
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
    public List<User> findAllById(List<UUID> ids) {
        return repository.findAllById(ids).stream()
                .map(JpaUserEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<User> findByUsername(Username username) {
        return repository.findByUsername(username.value()).map(JpaUserEntity::toDomain);
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
