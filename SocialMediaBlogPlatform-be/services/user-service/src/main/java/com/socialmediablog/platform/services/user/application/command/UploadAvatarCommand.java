package com.socialmediablog.platform.services.user.application.command;

import java.util.UUID;

public record UploadAvatarCommand(
        UUID userId,
        String originalFilename,
        String mimeType,
        byte[] content
) {
}
