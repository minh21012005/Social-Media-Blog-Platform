package com.socialmediablog.platform.services.notification.domain.repository;

import com.socialmediablog.platform.services.notification.domain.aggregate.Notification;
import com.socialmediablog.platform.services.notification.domain.vo.NotificationId;
import com.socialmediablog.platform.services.notification.domain.vo.RecipientId;
import java.util.List;
import java.util.Optional;
import java.time.Instant;

public interface NotificationRepository {

    Optional<Notification> findById(NotificationId id);

    List<Notification> findByRecipientId(RecipientId recipientId);

    Notification save(Notification notification);

    int markAllAsReadByRecipientId(RecipientId recipientId, Instant now);
}
