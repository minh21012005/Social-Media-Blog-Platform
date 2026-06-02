package com.socialmediablog.platform.services.article.api.dto;

import com.socialmediablog.platform.services.article.application.result.ArticleView;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ArticleResponse(
        UUID id,
        UUID authorId,
        String title,
        String slug,
        String category,
        String summary,
        String content,
        String coverImageUrl,
        String status,
        Instant publishedAt,
        Set<String> tags,
        Instant createdAt,
        Instant updatedAt,
        ArticleStatsResponse stats
) {

    public static ArticleResponse from(ArticleView article) {
        return new ArticleResponse(
                article.id(),
                article.authorId(),
                article.title(),
                article.slug(),
                article.category(),
                article.summary(),
                article.content(),
                article.coverImageUrl(),
                article.status(),
                article.publishedAt(),
                article.tags(),
                article.createdAt(),
                article.updatedAt(),
                ArticleStatsResponse.from(article.stats())
        );
    }
}
