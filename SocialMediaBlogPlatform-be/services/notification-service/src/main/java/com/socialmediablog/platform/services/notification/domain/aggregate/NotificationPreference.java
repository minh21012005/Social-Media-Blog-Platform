package com.socialmediablog.platform.services.notification.domain.aggregate;

import com.socialmediablog.platform.services.notification.domain.vo.NotificationPreferenceId;
import java.time.Instant;
import java.util.UUID;

public class NotificationPreference {

    private final NotificationPreferenceId id;
    private final UUID userId;
    private final boolean inAppEnabled;
    private final boolean emailEnabled;
    private final boolean commentNotificationsEnabled;
    private final boolean followerNotificationsEnabled;
    private final boolean clapNotificationsEnabled;
    private final boolean articleNotificationsEnabled;
    private final Instant createdAt;
    private final Instant updatedAt;

    private NotificationPreference(
            NotificationPreferenceId id,
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
        if (userId == null) {
            throw new IllegalArgumentException("Notification preference user id is required");
        }
        this.id = id;
        this.userId = userId;
        this.inAppEnabled = inAppEnabled;
        this.emailEnabled = emailEnabled;
        this.commentNotificationsEnabled = commentNotificationsEnabled;
        this.followerNotificationsEnabled = followerNotificationsEnabled;
        this.clapNotificationsEnabled = clapNotificationsEnabled;
        this.articleNotificationsEnabled = articleNotificationsEnabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static NotificationPreference defaults(UUID userId, Instant now) {
        return new NotificationPreference(
                NotificationPreferenceId.of(UUID.randomUUID()),
                userId,
                true,
                false,
                true,
                true,
                true,
                true,
                now,
                now
        );
    }

    public static NotificationPreference restore(
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
        return new NotificationPreference(
                NotificationPreferenceId.of(id),
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

    public NotificationPreferenceId id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public boolean inAppEnabled() {
        return inAppEnabled;
    }

    public boolean emailEnabled() {
        return emailEnabled;
    }

    public boolean commentNotificationsEnabled() {
        return commentNotificationsEnabled;
    }

    public boolean followerNotificationsEnabled() {
        return followerNotificationsEnabled;
    }

    public boolean clapNotificationsEnabled() {
        return clapNotificationsEnabled;
    }

    public boolean articleNotificationsEnabled() {
        return articleNotificationsEnabled;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
