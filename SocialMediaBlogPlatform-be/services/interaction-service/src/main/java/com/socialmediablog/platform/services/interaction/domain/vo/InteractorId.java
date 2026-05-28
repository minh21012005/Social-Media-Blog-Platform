package com.socialmediablog.platform.services.interaction.domain.vo;

import java.util.UUID;

public record InteractorId(UUID value) {

    public static InteractorId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Interactor id is required");
        }
        return new InteractorId(value);
    }
}
