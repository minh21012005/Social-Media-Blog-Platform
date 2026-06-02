package com.socialmediablog.platform.services.article.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record ArticleRequest(
        @NotBlank @Size(max = 180) String title,
        @Size(max = 220) @Pattern(regexp = "^[a-z0-9-]+$", message = "must contain lowercase letters, numbers, and dashes only") String slug,
        @NotBlank @Size(max = 30) String category,
        @Size(max = 500) String summary,
        @NotBlank String content,
        @Size(max = 2048) String coverImageUrl,
        Set<@Size(max = 50) String> tags
) {
}
