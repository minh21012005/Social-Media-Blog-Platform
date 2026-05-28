package com.socialmediablog.platform.services.article.infrastructure.entity;

import com.socialmediablog.platform.common.web.entity.BaseEntity;
import com.socialmediablog.platform.services.article.domain.aggregate.ArticleMediaAsset;
import com.socialmediablog.platform.services.article.domain.model.ArticleMediaType;
import com.socialmediablog.platform.services.article.domain.model.MediaAssetStatus;
import com.socialmediablog.platform.services.article.domain.model.MediaProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "article_media_assets",
        uniqueConstraints = @UniqueConstraint(name = "uk_article_media_assets_provider_public_id", columnNames = "provider_public_id")
)
public class JpaArticleMediaAssetEntity extends BaseEntity {

    @Column(name = "article_id")
    private UUID articleId;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

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

    protected JpaArticleMediaAssetEntity() {
    }

    private JpaArticleMediaAssetEntity(
            UUID id,
            UUID articleId,
            UUID ownerId,
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
        this.articleId = articleId;
        this.ownerId = ownerId;
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

    public static JpaArticleMediaAssetEntity fromDomain(ArticleMediaAsset mediaAsset) {
        return new JpaArticleMediaAssetEntity(
                mediaAsset.id().value(),
                mediaAsset.articleId() == null ? null : mediaAsset.articleId().value(),
                mediaAsset.ownerId(),
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

    public ArticleMediaAsset toDomain() {
        return ArticleMediaAsset.restore(
                id,
                articleId,
                ownerId,
                ArticleMediaType.valueOf(mediaType),
                MediaProvider.valueOf(provider),
                providerPublicId,
                secureUrl,
                originalFilename,
                mimeType,
                sizeBytes,
                width,
                height,
                MediaAssetStatus.valueOf(status),
                createdAt,
                updatedAt
        );
    }
}
