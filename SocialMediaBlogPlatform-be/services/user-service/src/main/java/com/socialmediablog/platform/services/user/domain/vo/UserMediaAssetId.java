package com.socialmediablog.platform.services.user.domain.vo;

import java.util.UUID;

public record UserMediaAssetId(UUID value) {

    public static UserMediaAssetId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("User media asset id is required");
        }
        return new UserMediaAssetId(value);
    }
}
