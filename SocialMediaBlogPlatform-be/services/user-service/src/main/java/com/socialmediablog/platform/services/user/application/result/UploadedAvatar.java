package com.socialmediablog.platform.services.user.application.result;

public record UploadedAvatar(
        UserProfile user,
        StoredUserMedia media
) {
}
