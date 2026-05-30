package com.socialmediablog.platform.services.article.application.result;

import java.util.UUID;

public record UploadedArticleMedia(
        UUID assetId,
        String secureUrl,
        String providerPublicId,
        String mimeType,
        long sizeBytes,
        Integer width,
        Integer height
) {
}
