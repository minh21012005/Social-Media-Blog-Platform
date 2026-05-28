package com.socialmediablog.platform.services.notification.domain.repository;

import com.socialmediablog.platform.services.notification.domain.aggregate.NotificationPreference;
import java.util.Optional;
import java.util.UUID;

public interface NotificationPreferenceRepository {

    Optional<NotificationPreference> findByUserId(UUID userId);

    NotificationPreference save(NotificationPreference preference);
}
