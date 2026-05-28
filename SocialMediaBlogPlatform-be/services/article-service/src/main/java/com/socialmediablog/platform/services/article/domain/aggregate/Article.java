package com.socialmediablog.platform.services.article.domain.aggregate;

import com.socialmediablog.platform.services.article.domain.model.ArticleStatus;
import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import com.socialmediablog.platform.services.article.domain.vo.ArticleTitle;
import com.socialmediablog.platform.services.article.domain.vo.AuthorId;
import com.socialmediablog.platform.services.article.domain.vo.Slug;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class Article {

    private final ArticleId id;
    private final AuthorId authorId;
    private final ArticleTitle title;
    private final Slug slug;
    private final String summary;
    private final String content;
    private final String coverImageUrl;
    private final ArticleStatus status;
    private final Instant publishedAt;
    private final Set<String> tags;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Article(
            ArticleId id,
            AuthorId authorId,
            ArticleTitle title,
            Slug slug,
            String summary,
            String content,
            String coverImageUrl,
            ArticleStatus status,
            Instant publishedAt,
            Set<String> tags,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.slug = slug;
        this.summary = normalizeSummary(summary);
        this.content = normalizeContent(content);
        this.coverImageUrl = normalizeCoverImageUrl(coverImageUrl);
        this.status = status;
        this.publishedAt = publishedAt;
        this.tags = Set.copyOf(tags == null ? Set.of() : tags);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Article draft(
            AuthorId authorId,
            ArticleTitle title,
            Slug slug,
            String summary,
            String content,
            String coverImageUrl,
            Set<String> tags,
            Instant now
    ) {
        return new Article(
                ArticleId.of(UUID.randomUUID()),
                authorId,
                title,
                slug,
                summary,
                content,
                coverImageUrl,
                ArticleStatus.DRAFT,
                null,
                normalizedTags(tags),
                now,
                now
        );
    }

    public static Article restore(
            UUID id,
            UUID authorId,
            String title,
            String slug,
            String summary,
            String content,
            String coverImageUrl,
            ArticleStatus status,
            Instant publishedAt,
            Set<String> tags,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Article(
                ArticleId.of(id),
                AuthorId.of(authorId),
                ArticleTitle.of(title),
                Slug.of(slug),
                summary,
                content,
                coverImageUrl,
                status,
                publishedAt,
                normalizedTags(tags),
                createdAt,
                updatedAt
        );
    }

    private static String normalizeSummary(String summary) {
        if (summary == null || summary.isBlank()) {
            return null;
        }
        String normalized = summary.trim();
        if (normalized.length() > 500) {
            throw new IllegalArgumentException("Article summary must not exceed 500 characters");
        }
        return normalized;
    }

    private static String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Article content is required");
        }
        return content.trim();
    }

    private static String normalizeCoverImageUrl(String coverImageUrl) {
        if (coverImageUrl == null || coverImageUrl.isBlank()) {
            return null;
        }
        String normalized = coverImageUrl.trim();
        if (normalized.length() > 2048) {
            throw new IllegalArgumentException("Cover image URL must not exceed 2048 characters");
        }
        return normalized;
    }

    private static Set<String> normalizedTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Set.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String tag : tags) {
            if (tag != null && !tag.isBlank()) {
                String value = tag.trim().toLowerCase();
                if (value.length() > 50) {
                    throw new IllegalArgumentException("Article tag must not exceed 50 characters");
                }
                normalized.add(value);
            }
        }
        return normalized;
    }

    public ArticleId id() {
        return id;
    }

    public AuthorId authorId() {
        return authorId;
    }

    public ArticleTitle title() {
        return title;
    }

    public Slug slug() {
        return slug;
    }

    public String summary() {
        return summary;
    }

    public String content() {
        return content;
    }

    public String coverImageUrl() {
        return coverImageUrl;
    }

    public ArticleStatus status() {
        return status;
    }

    public Instant publishedAt() {
        return publishedAt;
    }

    public Set<String> tags() {
        return tags;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
