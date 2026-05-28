package com.socialmediablog.platform.services.article.application.result;

public record StoredArticleMedia(
        String providerPublicId,
        String secureUrl,
        String originalFilename,
        String mimeType,
        long sizeBytes,
        Integer width,
        Integer height
) {
}
