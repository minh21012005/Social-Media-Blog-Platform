package com.socialmediablog.platform.services.article.api.dto;

import com.socialmediablog.platform.services.article.application.result.ArticleView;
import com.socialmediablog.platform.services.article.application.result.PageResult;
import java.util.List;

public record ArticlePageResponse(
        List<ArticleResponse> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {

    public static ArticlePageResponse from(PageResult<ArticleView> page) {
        return new ArticlePageResponse(
                page.items().stream().map(ArticleResponse::from).toList(),
                page.page(),
                page.size(),
                page.totalItems(),
                page.totalPages()
        );
    }
}
