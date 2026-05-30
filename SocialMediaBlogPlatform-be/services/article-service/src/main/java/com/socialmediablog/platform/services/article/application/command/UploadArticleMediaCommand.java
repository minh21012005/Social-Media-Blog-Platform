package com.socialmediablog.platform.services.article.application.command;

import java.util.UUID;

public record UploadArticleMediaCommand(
        UUID ownerId,
        String originalFilename,
        String mimeType,
        byte[] content
) {
}
