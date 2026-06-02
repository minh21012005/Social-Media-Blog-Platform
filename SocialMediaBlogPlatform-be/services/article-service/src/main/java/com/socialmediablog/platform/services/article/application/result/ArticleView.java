package com.socialmediablog.platform.services.article.application.result;

import com.socialmediablog.platform.services.article.domain.aggregate.Article;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ArticleView(
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
        Integer featuredRank,
        Integer editorPickRank,
        Set<String> tags,
        Instant createdAt,
        Instant updatedAt,
        ArticleStatsView stats
) {

    public static ArticleView from(Article article, ArticleStatsView stats) {
        return new ArticleView(
                article.id().value(),
                article.authorId().value(),
                article.title().value(),
                article.slug().value(),
                article.category().slug(),
                article.summary(),
                article.content(),
                article.coverImageUrl(),
                article.status().name(),
                article.publishedAt(),
                article.featuredRank(),
                article.editorPickRank(),
                article.tags(),
                article.createdAt(),
                article.updatedAt(),
                stats
        );
    }
}
