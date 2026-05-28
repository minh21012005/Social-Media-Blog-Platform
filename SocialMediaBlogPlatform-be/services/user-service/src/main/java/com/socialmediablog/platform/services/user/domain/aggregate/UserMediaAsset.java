package com.socialmediablog.platform.services.user.domain.aggregate;

import com.socialmediablog.platform.services.user.domain.model.UserMediaAssetStatus;
import com.socialmediablog.platform.services.user.domain.model.UserMediaProvider;
import com.socialmediablog.platform.services.user.domain.model.UserMediaType;
import com.socialmediablog.platform.services.user.domain.vo.UserMediaAssetId;
import java.time.Instant;
import java.util.UUID;

public class UserMediaAsset {

    private final UserMediaAssetId id;
    private final UUID userId;
    private final UserMediaType mediaType;
    private final UserMediaProvider provider;
    private final String providerPublicId;
    private final String secureUrl;
    private final String originalFilename;
    private final String mimeType;
    private final long sizeBytes;
    private final Integer width;
    private final Integer height;
    private final UserMediaAssetStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    private UserMediaAsset(
            UserMediaAssetId id,
            UUID userId,
            UserMediaType mediaType,
            UserMediaProvider provider,
            String providerPublicId,
            String secureUrl,
            String originalFilename,
            String mimeType,
            long sizeBytes,
            Integer width,
            Integer height,
            UserMediaAssetStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.userId = requireUserId(userId);
        this.mediaType = mediaType;
        this.provider = provider;
        this.providerPublicId = required(providerPublicId, "provider public id", 255);
        this.secureUrl = required(secureUrl, "secure url", 2048);
        this.originalFilename = optional(originalFilename, 255, "original filename");
        this.mimeType = optional(mimeType, 120, "mime type");
        this.sizeBytes = nonNegative(sizeBytes);
        this.width = positiveOrNull(width, "width");
        this.height = positiveOrNull(height, "height");
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserMediaAsset avatarUploaded(
            UUID userId,
            String providerPublicId,
            String secureUrl,
            String originalFilename,
            String mimeType,
            long sizeBytes,
            Integer width,
            Integer height,
            Instant now
    ) {
        return new UserMediaAsset(
                UserMediaAssetId.of(UUID.randomUUID()),
                userId,
                UserMediaType.AVATAR,
                UserMediaProvider.CLOUDINARY,
                providerPublicId,
                secureUrl,
                originalFilename,
                mimeType,
                sizeBytes,
                width,
                height,
                UserMediaAssetStatus.ACTIVE,
                now,
                now
        );
    }

    public static UserMediaAsset restore(
            UUID id,
            UUID userId,
            UserMediaType mediaType,
            UserMediaProvider provider,
            String providerPublicId,
            String secureUrl,
            String originalFilename,
            String mimeType,
            long sizeBytes,
            Integer width,
            Integer height,
            UserMediaAssetStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new UserMediaAsset(
                UserMediaAssetId.of(id),
                userId,
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

    private static UUID requireUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User media owner id is required");
        }
        return userId;
    }

    private static String required(String value, String field, int maxLength) {
        String normalized = optional(value, maxLength, field);
        if (normalized == null) {
            throw new IllegalArgumentException("User media " + field + " is required");
        }
        return normalized;
    }

    private static String optional(String value, int maxLength, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException("User media " + field + " must not exceed " + maxLength + " characters");
        }
        return normalized;
    }

    private static long nonNegative(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("User media size bytes must not be negative");
        }
        return value;
    }

    private static Integer positiveOrNull(Integer value, String field) {
        if (value != null && value < 1) {
            throw new IllegalArgumentException("User media " + field + " must be positive");
        }
        return value;
    }

    public UserMediaAssetId id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public UserMediaType mediaType() {
        return mediaType;
    }

    public UserMediaProvider provider() {
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

    public UserMediaAssetStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
