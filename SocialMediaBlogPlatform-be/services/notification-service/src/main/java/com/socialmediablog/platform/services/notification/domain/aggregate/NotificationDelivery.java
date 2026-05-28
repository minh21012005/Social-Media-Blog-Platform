package com.socialmediablog.platform.services.notification.domain.aggregate;

import com.socialmediablog.platform.services.notification.domain.model.NotificationChannel;
import com.socialmediablog.platform.services.notification.domain.model.NotificationDeliveryStatus;
import com.socialmediablog.platform.services.notification.domain.vo.NotificationDeliveryId;
import com.socialmediablog.platform.services.notification.domain.vo.NotificationId;
import java.time.Instant;
import java.util.UUID;

public class NotificationDelivery {

    private final NotificationDeliveryId id;
    private final NotificationId notificationId;
    private final NotificationChannel channel;
    private final NotificationDeliveryStatus status;
    private final Instant deliveredAt;
    private final Instant failedAt;
    private final String failureReason;
    private final Instant createdAt;
    private final Instant updatedAt;

    private NotificationDelivery(
            NotificationDeliveryId id,
            NotificationId notificationId,
            NotificationChannel channel,
            NotificationDeliveryStatus status,
            Instant deliveredAt,
            Instant failedAt,
            String failureReason,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.notificationId = notificationId;
        this.channel = channel;
        this.status = status;
        this.deliveredAt = deliveredAt;
        this.failedAt = failedAt;
        this.failureReason = optional(failureReason, 500);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static NotificationDelivery pending(NotificationId notificationId, NotificationChannel channel, Instant now) {
        return new NotificationDelivery(
                NotificationDeliveryId.of(UUID.randomUUID()),
                notificationId,
                channel,
                NotificationDeliveryStatus.PENDING,
                null,
                null,
                null,
                now,
                now
        );
    }

    public static NotificationDelivery restore(
            UUID id,
            UUID notificationId,
            NotificationChannel channel,
            NotificationDeliveryStatus status,
            Instant deliveredAt,
            Instant failedAt,
            String failureReason,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new NotificationDelivery(
                NotificationDeliveryId.of(id),
                NotificationId.of(notificationId),
                channel,
                status,
                deliveredAt,
                failedAt,
                failureReason,
                createdAt,
                updatedAt
        );
    }

    private static String optional(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException("Notification delivery failure reason must not exceed " + maxLength + " characters");
        }
        return normalized;
    }

    public NotificationDeliveryId id() {
        return id;
    }

    public NotificationId notificationId() {
        return notificationId;
    }

    public NotificationChannel channel() {
        return channel;
    }

    public NotificationDeliveryStatus status() {
        return status;
    }

    public Instant deliveredAt() {
        return deliveredAt;
    }

    public Instant failedAt() {
        return failedAt;
    }

    public String failureReason() {
        return failureReason;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
