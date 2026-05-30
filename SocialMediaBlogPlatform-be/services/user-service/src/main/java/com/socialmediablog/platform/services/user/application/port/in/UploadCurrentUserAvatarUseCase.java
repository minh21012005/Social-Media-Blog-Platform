package com.socialmediablog.platform.services.user.application.port.in;

import com.socialmediablog.platform.services.user.application.command.UploadAvatarCommand;
import com.socialmediablog.platform.services.user.application.result.UploadedAvatar;

public interface UploadCurrentUserAvatarUseCase {

    UploadedAvatar execute(UploadAvatarCommand command);
}
