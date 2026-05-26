package com.socialmediablog.platform.services.user.application.port;

import com.socialmediablog.platform.services.user.domain.EmailAddress;
import com.socialmediablog.platform.services.user.domain.User;
import com.socialmediablog.platform.services.user.domain.Username;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    boolean existsByUsername(Username username);

    boolean existsByEmail(EmailAddress email);

    Optional<User> findById(UUID id);

    Optional<User> findByEmailOrUsername(String identifier);

    User save(User user);
}
