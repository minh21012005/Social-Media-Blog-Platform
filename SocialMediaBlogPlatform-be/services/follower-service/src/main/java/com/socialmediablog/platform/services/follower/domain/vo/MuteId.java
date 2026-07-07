package com.socialmediablog.platform.services.follower.domain.vo;

import java.util.UUID;

public record MuteId(UUID value) {
    public static MuteId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Mute ID must not be null");
        }
        return new MuteId(value);
    }
}
