package com.socialmediablog.platform.services.article.api.dto;

public record CurateArticleRequest(
        Integer featuredRank,
        Integer editorPickRank
) {
}
