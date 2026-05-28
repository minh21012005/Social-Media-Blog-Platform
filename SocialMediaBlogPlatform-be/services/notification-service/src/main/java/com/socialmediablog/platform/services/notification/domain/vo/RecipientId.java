package com.socialmediablog.platform.services.notification.domain.vo;

import java.util.UUID;

public record RecipientId(UUID value) {

    public static RecipientId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Notification recipient id is required");
        }
        return new RecipientId(value);
    }
}
