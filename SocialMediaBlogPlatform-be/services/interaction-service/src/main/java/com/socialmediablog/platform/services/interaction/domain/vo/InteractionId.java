package com.socialmediablog.platform.services.interaction.domain.vo;

import java.util.UUID;

public record InteractionId(UUID value) {

    public static InteractionId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Interaction id is required");
        }
        return new InteractionId(value);
    }
}
