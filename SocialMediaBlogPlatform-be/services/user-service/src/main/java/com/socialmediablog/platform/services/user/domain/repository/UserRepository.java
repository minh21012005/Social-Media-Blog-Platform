package com.socialmediablog.platform.services.user.domain.repository;

import com.socialmediablog.platform.services.user.domain.vo.EmailAddress;
import com.socialmediablog.platform.services.user.domain.aggregate.User;
import com.socialmediablog.platform.services.user.domain.vo.Username;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    boolean existsByUsername(Username username);

    boolean existsByEmail(EmailAddress email);

    Optional<User> findById(UUID id);

    List<User> findAllById(List<UUID> ids);

    Optional<User> findByUsername(Username username);

    Optional<User> findByEmailOrUsername(String identifier);

    User save(User user);
}
