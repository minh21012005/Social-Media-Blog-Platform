package com.socialmediablog.platform.services.notification.infrastructure.messaging;

import java.time.Instant;
import java.util.UUID;

public record NotificationPushEvent(
        UUID id,
        UUID actorId,
        UUID recipientId,
        String type,
        String subjectType,
        UUID subjectId,
        String title,
        String body,
        String status,
        Instant createdAt
) {
}
