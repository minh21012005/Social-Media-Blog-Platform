package com.socialmediablog.platform.services.notification.infrastructure.entity;

import com.socialmediablog.platform.common.web.entity.BaseEntity;
import com.socialmediablog.platform.services.notification.domain.aggregate.Notification;
import com.socialmediablog.platform.services.notification.domain.model.NotificationStatus;
import com.socialmediablog.platform.services.notification.domain.model.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class JpaNotificationEntity extends BaseEntity {

    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(nullable = false, length = 40)
    private String type;

    @Column(name = "subject_type", length = 60)
    private String subjectType;

    @Column(name = "subject_id")
    private UUID subjectId;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(length = 1000)
    private String body;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "read_at")
    private Instant readAt;

    protected JpaNotificationEntity() {
    }

    private JpaNotificationEntity(
            UUID id,
            UUID recipientId,
            UUID actorId,
            String type,
            String subjectType,
            UUID subjectId,
            String title,
            String body,
            String status,
            Instant readAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        super(id, createdAt, updatedAt);
        this.recipientId = recipientId;
        this.actorId = actorId;
        this.type = type;
        this.subjectType = subjectType;
        this.subjectId = subjectId;
        this.title = title;
        this.body = body;
        this.status = status;
        this.readAt = readAt;
    }

    public static JpaNotificationEntity fromDomain(Notification notification) {
        return new JpaNotificationEntity(
                notification.id().value(),
                notification.recipientId().value(),
                notification.actorId(),
                notification.type().name(),
                notification.subjectType(),
                notification.subjectId(),
                notification.title(),
                notification.body(),
                notification.status().name(),
                notification.readAt(),
                notification.createdAt(),
                notification.updatedAt()
        );
    }

    public Notification toDomain() {
        return Notification.restore(
                id,
                recipientId,
                actorId,
                NotificationType.valueOf(type),
                subjectType,
                subjectId,
                title,
                body,
                NotificationStatus.valueOf(status),
                readAt,
                createdAt,
                updatedAt
        );
    }
}
