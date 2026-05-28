package com.socialmediablog.platform.services.user.infrastructure.entity;

import com.socialmediablog.platform.common.web.entity.BaseEntity;
import com.socialmediablog.platform.services.user.domain.aggregate.UserMediaAsset;
import com.socialmediablog.platform.services.user.domain.model.UserMediaAssetStatus;
import com.socialmediablog.platform.services.user.domain.model.UserMediaProvider;
import com.socialmediablog.platform.services.user.domain.model.UserMediaType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "user_media_assets",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_media_assets_provider_public_id", columnNames = "provider_public_id")
)
public class JpaUserMediaAssetEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "media_type", nullable = false, length = 30)
    private String mediaType;

    @Column(nullable = false, length = 30)
    private String provider;

    @Column(name = "provider_public_id", nullable = false, length = 255)
    private String providerPublicId;

    @Column(name = "secure_url", nullable = false, length = 2048)
    private String secureUrl;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "mime_type", length = 120)
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    private Integer width;

    private Integer height;

    @Column(nullable = false, length = 20)
    private String status;

    protected JpaUserMediaAssetEntity() {
    }

    private JpaUserMediaAssetEntity(
            UUID id,
            UUID userId,
            String mediaType,
            String provider,
            String providerPublicId,
            String secureUrl,
            String originalFilename,
            String mimeType,
            long sizeBytes,
            Integer width,
            Integer height,
            String status,
            Instant createdAt,
            Instant updatedAt
    ) {
        super(id, createdAt, updatedAt);
        this.userId = userId;
        this.mediaType = mediaType;
        this.provider = provider;
        this.providerPublicId = providerPublicId;
        this.secureUrl = secureUrl;
        this.originalFilename = originalFilename;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.width = width;
        this.height = height;
        this.status = status;
    }

    public static JpaUserMediaAssetEntity fromDomain(UserMediaAsset mediaAsset) {
        return new JpaUserMediaAssetEntity(
                mediaAsset.id().value(),
                mediaAsset.userId(),
                mediaAsset.mediaType().name(),
                mediaAsset.provider().name(),
                mediaAsset.providerPublicId(),
                mediaAsset.secureUrl(),
                mediaAsset.originalFilename(),
                mediaAsset.mimeType(),
                mediaAsset.sizeBytes(),
                mediaAsset.width(),
                mediaAsset.height(),
                mediaAsset.status().name(),
                mediaAsset.createdAt(),
                mediaAsset.updatedAt()
        );
    }

    public UserMediaAsset toDomain() {
        return UserMediaAsset.restore(
                id,
                userId,
                UserMediaType.valueOf(mediaType),
                UserMediaProvider.valueOf(provider),
                providerPublicId,
                secureUrl,
                originalFilename,
                mimeType,
                sizeBytes,
                width,
                height,
                UserMediaAssetStatus.valueOf(status),
                createdAt,
                updatedAt
        );
    }
}
