package com.socialmediablog.platform.services.article.api.dto;

import jakarta.validation.constraints.Size;

public record RecordArticleViewRequest(
        @Size(max = 120) String anonymousViewerKey,
        @Size(max = 80) String source
) {
}
