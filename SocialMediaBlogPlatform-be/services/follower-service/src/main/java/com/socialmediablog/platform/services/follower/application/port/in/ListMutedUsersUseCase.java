package com.socialmediablog.platform.services.follower.application.port.in;

import com.socialmediablog.platform.services.follower.application.result.FollowUserPage;
import java.util.UUID;

public interface ListMutedUsersUseCase {
    FollowUserPage listMuted(UUID muterId, int page, int size);
}
