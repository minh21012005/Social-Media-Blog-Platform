package com.socialmediablog.platform.services.notification.domain.vo;

import java.util.UUID;

public record NotificationPreferenceId(UUID value) {

    public static NotificationPreferenceId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Notification preference id is required");
        }
        return new NotificationPreferenceId(value);
    }
}
