package com.socialmediablog.platform.services.notification.infrastructure.entity;

import com.socialmediablog.platform.common.web.entity.BaseEntity;
import com.socialmediablog.platform.services.notification.domain.aggregate.NotificationDelivery;
import com.socialmediablog.platform.services.notification.domain.model.NotificationChannel;
import com.socialmediablog.platform.services.notification.domain.model.NotificationDeliveryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_deliveries")
public class JpaNotificationDeliveryEntity extends BaseEntity {

    @Column(name = "notification_id", nullable = false)
    private UUID notificationId;

    @Column(nullable = false, length = 20)
    private String channel;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    protected JpaNotificationDeliveryEntity() {
    }

    private JpaNotificationDeliveryEntity(
            UUID id,
            UUID notificationId,
            String channel,
            String status,
            Instant deliveredAt,
            Instant failedAt,
            String failureReason,
            Instant createdAt,
            Instant updatedAt
    ) {
        super(id, createdAt, updatedAt);
        this.notificationId = notificationId;
        this.channel = channel;
        this.status = status;
        this.deliveredAt = deliveredAt;
        this.failedAt = failedAt;
        this.failureReason = failureReason;
    }

    public static JpaNotificationDeliveryEntity fromDomain(NotificationDelivery delivery) {
        return new JpaNotificationDeliveryEntity(
                delivery.id().value(),
                delivery.notificationId().value(),
                delivery.channel().name(),
                delivery.status().name(),
                delivery.deliveredAt(),
                delivery.failedAt(),
                delivery.failureReason(),
                delivery.createdAt(),
                delivery.updatedAt()
        );
    }

    public NotificationDelivery toDomain() {
        return NotificationDelivery.restore(
                id,
                notificationId,
                NotificationChannel.valueOf(channel),
                NotificationDeliveryStatus.valueOf(status),
                deliveredAt,
                failedAt,
                failureReason,
                createdAt,
                updatedAt
        );
    }
}
