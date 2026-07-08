package com.socialmediablog.platform.services.article.domain.aggregate;

import com.socialmediablog.platform.services.article.domain.model.ArticleCategory;
import com.socialmediablog.platform.services.article.domain.model.ArticleStatus;
import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import com.socialmediablog.platform.services.article.domain.vo.ArticleTitle;
import com.socialmediablog.platform.services.article.domain.vo.AuthorId;
import com.socialmediablog.platform.services.article.domain.vo.Slug;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Article {

    private static final int MAX_CONTENT_IMAGES = 10;
    private static final int MAX_CONTENT_LENGTH = 50_000;
    private static final Pattern MARKDOWN_IMAGE_PATTERN = Pattern.compile("!\\[[^\\]]*]\\([^\\s)]+\\)");

    private final ArticleId id;
    private final AuthorId authorId;
    private final ArticleTitle title;
    private final Slug slug;
    private final ArticleCategory category;
    private final String summary;
    private final String content;
    private final String coverImageUrl;
    private final ArticleStatus status;
    private final Instant publishedAt;
    private final Integer featuredRank;
    private final Integer editorPickRank;
    private final Set<String> tags;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Article(
            ArticleId id,
            AuthorId authorId,
            ArticleTitle title,
            Slug slug,
            ArticleCategory category,
            String summary,
            String content,
            String coverImageUrl,
            ArticleStatus status,
            Instant publishedAt,
            Integer featuredRank,
            Integer editorPickRank,
            Set<String> tags,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.slug = slug;
        this.category = category;
        this.summary = normalizeSummary(summary);
        this.content = normalizeContent(content);
        this.coverImageUrl = normalizeCoverImageUrl(coverImageUrl);
        this.status = status;
        this.publishedAt = publishedAt;
        this.featuredRank = normalizeRank(featuredRank, "featured rank");
        this.editorPickRank = normalizeRank(editorPickRank, "editor pick rank");
        this.tags = Set.copyOf(tags == null ? Set.of() : tags);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Article draft(
            AuthorId authorId,
            ArticleTitle title,
            Slug slug,
            ArticleCategory category,
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
                category,
                summary,
                content,
                coverImageUrl,
                ArticleStatus.DRAFT,
                null,
                null,
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
            ArticleCategory category,
            String summary,
            String content,
            String coverImageUrl,
            ArticleStatus status,
            Instant publishedAt,
            Integer featuredRank,
            Integer editorPickRank,
            Set<String> tags,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Article(
                ArticleId.of(id),
                AuthorId.of(authorId),
                ArticleTitle.of(title),
                Slug.of(slug),
                category,
                summary,
                content,
                coverImageUrl,
                status,
                publishedAt,
                featuredRank,
                editorPickRank,
                normalizedTags(tags),
                createdAt,
                updatedAt
        );
    }

    public Article update(
            AuthorId actorId,
            ArticleTitle title,
            Slug slug,
            ArticleCategory category,
            String summary,
            String content,
            String coverImageUrl,
            Set<String> tags,
            Instant now
    ) {
        ensureOwner(actorId);
        if (status == ArticleStatus.DELETED) {
            throw new IllegalStateException("Deleted articles cannot be updated");
        }
        return new Article(id, authorId, title, slug, category, summary, content, coverImageUrl, status, publishedAt,
                featuredRank, editorPickRank,
                normalizedTags(tags), createdAt, now);
    }

    public Article publish(AuthorId actorId, Instant now) {
        ensureOwner(actorId);
        if (status == ArticleStatus.DELETED) {
            throw new IllegalStateException("Deleted articles cannot be published");
        }
        validatePublishable();
        Instant nextPublishedAt = this.publishedAt != null ? this.publishedAt : now;
        return new Article(id, authorId, title, slug, category, summary, content, coverImageUrl,
                ArticleStatus.PUBLISHED, nextPublishedAt, featuredRank, editorPickRank, tags, createdAt, now);
    }

    public Article archive(AuthorId actorId, Instant now) {
        ensureOwner(actorId);
        if (status == ArticleStatus.DELETED) {
            throw new IllegalStateException("Deleted articles cannot be archived");
        }
        if (status == ArticleStatus.ARCHIVED) {
            return this;
        }
        return new Article(id, authorId, title, slug, category, summary, content, coverImageUrl,
                ArticleStatus.ARCHIVED, publishedAt, null, null, tags, createdAt, now);
    }

    public Article delete(AuthorId actorId, Instant now) {
        ensureOwner(actorId);
        if (status == ArticleStatus.PUBLISHED) {
            throw new IllegalStateException("Published articles must be archived before deleting");
        }
        if (status == ArticleStatus.DELETED) {
            return this;
        }
        return new Article(id, authorId, title, slug, category, summary, content, coverImageUrl,
                ArticleStatus.DELETED, publishedAt, null, null, tags, createdAt, now);
    }

    public Article curate(Integer featuredRank, Integer editorPickRank, Instant now) {
        return new Article(id, authorId, title, slug, category, summary, content, coverImageUrl,
                status, publishedAt, featuredRank, editorPickRank, tags, createdAt, now);
    }

    public boolean isPublished() {
        return status == ArticleStatus.PUBLISHED;
    }

    public void ensureOwner(AuthorId actorId) {
        if (actorId == null || !authorId.equals(actorId)) {
            throw new IllegalStateException("Only the article author can perform this action");
        }
    }

    private void validatePublishable() {
        if (summary == null || summary.isBlank()) {
            throw new IllegalStateException("Published article summary is required");
        }
        if (coverImageUrl == null || coverImageUrl.isBlank()) {
            throw new IllegalStateException("Published article cover image is required");
        }
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
        String normalized = content.trim();
        if (normalized.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("Article content must not exceed " + MAX_CONTENT_LENGTH + " characters");
        }
        int imageCount = markdownImageCount(normalized);
        if (imageCount > MAX_CONTENT_IMAGES) {
            throw new IllegalArgumentException("Article content can include up to " + MAX_CONTENT_IMAGES + " images");
        }
        return normalized;
    }

    private static int markdownImageCount(String content) {
        Matcher matcher = MARKDOWN_IMAGE_PATTERN.matcher(content);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
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

    private static Integer normalizeRank(Integer rank, String field) {
        if (rank == null) {
            return null;
        }
        if (rank < 1) {
            throw new IllegalArgumentException("Article " + field + " must be at least 1");
        }
        return rank;
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

    public ArticleCategory category() {
        return category;
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

    public Integer featuredRank() {
        return featuredRank;
    }

    public Integer editorPickRank() {
        return editorPickRank;
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
