package com.socialmediablog.platform.services.article.domain.aggregate;

import com.socialmediablog.platform.services.article.domain.model.ArticleMediaType;
import com.socialmediablog.platform.services.article.domain.model.MediaAssetStatus;
import com.socialmediablog.platform.services.article.domain.model.MediaProvider;
import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import com.socialmediablog.platform.services.article.domain.vo.ArticleMediaAssetId;
import java.time.Instant;
import java.util.UUID;

public class ArticleMediaAsset {

    private final ArticleMediaAssetId id;
    private final ArticleId articleId;
    private final UUID ownerId;
    private final ArticleMediaType mediaType;
    private final MediaProvider provider;
    private final String providerPublicId;
    private final String secureUrl;
    private final String originalFilename;
    private final String mimeType;
    private final long sizeBytes;
    private final Integer width;
    private final Integer height;
    private final MediaAssetStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    private ArticleMediaAsset(
            ArticleMediaAssetId id,
            ArticleId articleId,
            UUID ownerId,
            ArticleMediaType mediaType,
            MediaProvider provider,
            String providerPublicId,
            String secureUrl,
            String originalFilename,
            String mimeType,
            long sizeBytes,
            Integer width,
            Integer height,
            MediaAssetStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.articleId = articleId;
        this.ownerId = ownerId;
        this.mediaType = mediaType;
        this.provider = provider;
        this.providerPublicId = required(providerPublicId, "provider public id", 255);
        this.secureUrl = required(secureUrl, "secure url", 2048);
        this.originalFilename = optional(originalFilename, 255, "original filename");
        this.mimeType = optional(mimeType, 120, "mime type");
        this.sizeBytes = nonNegative(sizeBytes, "size bytes");
        this.width = positiveOrNull(width, "width");
        this.height = positiveOrNull(height, "height");
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ArticleMediaAsset uploaded(
            ArticleId articleId,
            UUID ownerId,
            ArticleMediaType mediaType,
            String providerPublicId,
            String secureUrl,
            String originalFilename,
            String mimeType,
            long sizeBytes,
            Integer width,
            Integer height,
            Instant now
    ) {
        return new ArticleMediaAsset(
                ArticleMediaAssetId.of(UUID.randomUUID()),
                articleId,
                ownerId,
                mediaType,
                MediaProvider.CLOUDINARY,
                providerPublicId,
                secureUrl,
                originalFilename,
                mimeType,
                sizeBytes,
                width,
                height,
                MediaAssetStatus.ACTIVE,
                now,
                now
        );
    }

    public static ArticleMediaAsset restore(
            UUID id,
            UUID articleId,
            UUID ownerId,
            ArticleMediaType mediaType,
            MediaProvider provider,
            String providerPublicId,
            String secureUrl,
            String originalFilename,
            String mimeType,
            long sizeBytes,
            Integer width,
            Integer height,
            MediaAssetStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new ArticleMediaAsset(
                ArticleMediaAssetId.of(id),
                articleId == null ? null : ArticleId.of(articleId),
                ownerId,
                mediaType,
                provider,
                providerPublicId,
                secureUrl,
                originalFilename,
                mimeType,
                sizeBytes,
                width,
                height,
                status,
                createdAt,
                updatedAt
        );
    }

    private static String required(String value, String field, int maxLength) {
        String normalized = optional(value, maxLength, field);
        if (normalized == null) {
            throw new IllegalArgumentException("Article media " + field + " is required");
        }
        return normalized;
    }

    private static String optional(String value, int maxLength, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException("Article media " + field + " must not exceed " + maxLength + " characters");
        }
        return normalized;
    }

    private static long nonNegative(long value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException("Article media " + field + " must not be negative");
        }
        return value;
    }

    private static Integer positiveOrNull(Integer value, String field) {
        if (value != null && value < 1) {
            throw new IllegalArgumentException("Article media " + field + " must be positive");
        }
        return value;
    }

    public ArticleMediaAssetId id() {
        return id;
    }

    public ArticleId articleId() {
        return articleId;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public ArticleMediaType mediaType() {
        return mediaType;
    }

    public MediaProvider provider() {
        return provider;
    }

    public String providerPublicId() {
        return providerPublicId;
    }

    public String secureUrl() {
        return secureUrl;
    }

    public String originalFilename() {
        return originalFilename;
    }

    public String mimeType() {
        return mimeType;
    }

    public long sizeBytes() {
        return sizeBytes;
    }

    public Integer width() {
        return width;
    }

    public Integer height() {
        return height;
    }

    public MediaAssetStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
