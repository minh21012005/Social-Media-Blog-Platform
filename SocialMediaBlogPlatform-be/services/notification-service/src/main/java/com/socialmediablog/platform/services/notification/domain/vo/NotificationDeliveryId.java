package com.socialmediablog.platform.services.notification.domain.vo;

import java.util.UUID;

public record NotificationDeliveryId(UUID value) {

    public static NotificationDeliveryId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Notification delivery id is required");
        }
        return new NotificationDeliveryId(value);
    }
}
