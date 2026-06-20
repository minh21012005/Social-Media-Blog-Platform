package com.socialmediablog.platform.services.notification.infrastructure.messaging;

import java.time.Instant;
import java.util.UUID;

public record NotificationPushEvent(
        UUID notificationId,
        UUID recipientId,
        String type,
        String title,
        String message,
        Instant createdAt
) {
}
