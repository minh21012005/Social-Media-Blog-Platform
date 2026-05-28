package com.socialmediablog.platform.services.interaction.domain.vo;

import java.util.UUID;

public record TargetId(UUID value) {

    public static TargetId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Interaction target id is required");
        }
        return new TargetId(value);
    }
}
