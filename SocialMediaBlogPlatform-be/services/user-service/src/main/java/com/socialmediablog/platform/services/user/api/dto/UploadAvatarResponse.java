package com.socialmediablog.platform.services.user.api.dto;

import com.socialmediablog.platform.services.user.application.result.UploadedAvatar;

public record UploadAvatarResponse(
        UserProfileResponse user,
        String secureUrl,
        String providerPublicId,
        String mimeType,
        long sizeBytes,
        Integer width,
        Integer height
) {

    public static UploadAvatarResponse from(UploadedAvatar uploadedAvatar) {
        return new UploadAvatarResponse(
                UserProfileResponse.from(uploadedAvatar.user()),
                uploadedAvatar.media().secureUrl(),
                uploadedAvatar.media().providerPublicId(),
                uploadedAvatar.media().mimeType(),
                uploadedAvatar.media().sizeBytes(),
                uploadedAvatar.media().width(),
                uploadedAvatar.media().height()
        );
    }
}
