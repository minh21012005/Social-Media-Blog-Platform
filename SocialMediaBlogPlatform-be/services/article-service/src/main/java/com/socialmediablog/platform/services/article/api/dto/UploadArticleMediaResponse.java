package com.socialmediablog.platform.services.article.api.dto;

import com.socialmediablog.platform.services.article.application.result.UploadedArticleMedia;
import java.util.UUID;

public record UploadArticleMediaResponse(
        UUID assetId,
        String secureUrl,
        String providerPublicId,
        String mimeType,
        long sizeBytes,
        Integer width,
        Integer height
) {

    public static UploadArticleMediaResponse from(UploadedArticleMedia media) {
        return new UploadArticleMediaResponse(
                media.assetId(),
                media.secureUrl(),
                media.providerPublicId(),
                media.mimeType(),
                media.sizeBytes(),
                media.width(),
                media.height()
        );
    }
}
