package com.socialmediablog.platform.services.user.application.port.out;

import com.socialmediablog.platform.services.user.application.result.StoredUserMedia;

public interface UserMediaStorage {

    StoredUserMedia uploadAvatar(String originalFilename, String mimeType, byte[] content);
}
