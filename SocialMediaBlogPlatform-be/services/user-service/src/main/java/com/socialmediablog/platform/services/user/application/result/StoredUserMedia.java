package com.socialmediablog.platform.services.user.application.result;

public record StoredUserMedia(
        String providerPublicId,
        String secureUrl,
        String originalFilename,
        String mimeType,
        long sizeBytes,
        Integer width,
        Integer height
) {
}
