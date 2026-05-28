package com.socialmediablog.platform.services.notification.domain.aggregate;

import com.socialmediablog.platform.services.notification.domain.model.NotificationStatus;
import com.socialmediablog.platform.services.notification.domain.model.NotificationType;
import com.socialmediablog.platform.services.notification.domain.vo.NotificationId;
import com.socialmediablog.platform.services.notification.domain.vo.RecipientId;
import java.time.Instant;
import java.util.UUID;

public class Notification {

    private final NotificationId id;
    private final RecipientId recipientId;
    private final UUID actorId;
    private final NotificationType type;
    private final String subjectType;
    private final UUID subjectId;
    private final String title;
    private final String body;
    private final NotificationStatus status;
    private final Instant readAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Notification(
            NotificationId id,
            RecipientId recipientId,
            UUID actorId,
            NotificationType type,
            String subjectType,
            UUID subjectId,
            String title,
            String body,
            NotificationStatus status,
            Instant readAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.recipientId = recipientId;
        this.actorId = actorId;
        this.type = type;
        this.subjectType = optional(subjectType, 60, "subject type");
        this.subjectId = subjectId;
        this.title = required(title, 180, "title");
        this.body = optional(body, 1000, "body");
        this.status = status;
        this.readAt = readAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Notification create(
            RecipientId recipientId,
            UUID actorId,
            NotificationType type,
            String subjectType,
            UUID subjectId,
            String title,
            String body,
            Instant now
    ) {
        return new Notification(
                NotificationId.of(UUID.randomUUID()),
                recipientId,
                actorId,
                type,
                subjectType,
                subjectId,
                title,
                body,
                NotificationStatus.UNREAD,
                null,
                now,
                now
        );
    }

    public static Notification restore(
            UUID id,
            UUID recipientId,
            UUID actorId,
            NotificationType type,
            String subjectType,
            UUID subjectId,
            String title,
            String body,
            NotificationStatus status,
            Instant readAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Notification(
                NotificationId.of(id),
                RecipientId.of(recipientId),
                actorId,
                type,
                subjectType,
                subjectId,
                title,
                body,
                status,
                readAt,
                createdAt,
                updatedAt
        );
    }

    private static String required(String value, int maxLength, String field) {
        String normalized = optional(value, maxLength, field);
        if (normalized == null) {
            throw new IllegalArgumentException("Notification " + field + " is required");
        }
        return normalized;
    }

    private static String optional(String value, int maxLength, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException("Notification " + field + " must not exceed " + maxLength + " characters");
        }
        return normalized;
    }

    public NotificationId id() {
        return id;
    }

    public RecipientId recipientId() {
        return recipientId;
    }

    public UUID actorId() {
        return actorId;
    }

    public NotificationType type() {
        return type;
    }

    public String subjectType() {
        return subjectType;
    }

    public UUID subjectId() {
        return subjectId;
    }

    public String title() {
        return title;
    }

    public String body() {
        return body;
    }

    public NotificationStatus status() {
        return status;
    }

    public Instant readAt() {
        return readAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
