package com.socialmediablog.platform.services.notification.domain.vo;

import java.util.UUID;

public record NotificationId(UUID value) {

    public static NotificationId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Notification id is required");
        }
        return new NotificationId(value);
    }
}
