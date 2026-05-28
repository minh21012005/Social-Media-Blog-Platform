package com.socialmediablog.platform.services.notification.infrastructure.entity;

import com.socialmediablog.platform.common.web.entity.BaseEntity;
import com.socialmediablog.platform.services.notification.domain.aggregate.NotificationPreference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_preferences")
public class JpaNotificationPreferenceEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "in_app_enabled", nullable = false)
    private boolean inAppEnabled;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled;

    @Column(name = "comment_notifications_enabled", nullable = false)
    private boolean commentNotificationsEnabled;

    @Column(name = "follower_notifications_enabled", nullable = false)
    private boolean followerNotificationsEnabled;

    @Column(name = "clap_notifications_enabled", nullable = false)
    private boolean clapNotificationsEnabled;

    @Column(name = "article_notifications_enabled", nullable = false)
    private boolean articleNotificationsEnabled;

    protected JpaNotificationPreferenceEntity() {
    }

    private JpaNotificationPreferenceEntity(
            UUID id,
            UUID userId,
            boolean inAppEnabled,
            boolean emailEnabled,
            boolean commentNotificationsEnabled,
            boolean followerNotificationsEnabled,
            boolean clapNotificationsEnabled,
            boolean articleNotificationsEnabled,
            Instant createdAt,
            Instant updatedAt
    ) {
        super(id, createdAt, updatedAt);
        this.userId = userId;
        this.inAppEnabled = inAppEnabled;
        this.emailEnabled = emailEnabled;
        this.commentNotificationsEnabled = commentNotificationsEnabled;
        this.followerNotificationsEnabled = followerNotificationsEnabled;
        this.clapNotificationsEnabled = clapNotificationsEnabled;
        this.articleNotificationsEnabled = articleNotificationsEnabled;
    }

    public static JpaNotificationPreferenceEntity fromDomain(NotificationPreference preference) {
        return new JpaNotificationPreferenceEntity(
                preference.id().value(),
                preference.userId(),
                preference.inAppEnabled(),
                preference.emailEnabled(),
                preference.commentNotificationsEnabled(),
                preference.followerNotificationsEnabled(),
                preference.clapNotificationsEnabled(),
                preference.articleNotificationsEnabled(),
                preference.createdAt(),
                preference.updatedAt()
        );
    }

    public NotificationPreference toDomain() {
        return NotificationPreference.restore(
                id,
                userId,
                inAppEnabled,
                emailEnabled,
                commentNotificationsEnabled,
                followerNotificationsEnabled,
                clapNotificationsEnabled,
                articleNotificationsEnabled,
                createdAt,
                updatedAt
        );
    }
}
