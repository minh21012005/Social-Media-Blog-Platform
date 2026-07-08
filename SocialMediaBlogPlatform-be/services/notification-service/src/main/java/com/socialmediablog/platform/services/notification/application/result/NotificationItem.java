package com.socialmediablog.platform.services.notification.application.result;

import com.socialmediablog.platform.services.notification.domain.aggregate.Notification;
import com.socialmediablog.platform.services.notification.domain.model.NotificationStatus;
import com.socialmediablog.platform.services.notification.domain.model.NotificationType;
import java.time.Instant;
import java.util.UUID;

public record NotificationItem(
        UUID id,
        UUID actorId,
        NotificationType type,
        String subjectType,
        UUID subjectId,
        String title,
        String body,
        NotificationStatus status,
        Instant readAt,
        Instant createdAt
) {
    public static NotificationItem from(Notification n) {
        return new NotificationItem(
                n.id().value(),
                n.actorId(),
                n.type(),
                n.subjectType(),
                n.subjectId(),
                n.title(),
                n.body(),
                n.status(),
                n.readAt(),
                n.createdAt()
        );
    }
}
