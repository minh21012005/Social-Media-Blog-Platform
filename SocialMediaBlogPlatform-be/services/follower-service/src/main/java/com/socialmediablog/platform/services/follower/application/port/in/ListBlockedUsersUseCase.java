package com.socialmediablog.platform.services.follower.application.port.in;

import com.socialmediablog.platform.services.follower.application.command.ListBlockedUsersCommand;
import com.socialmediablog.platform.services.follower.application.result.FollowUserPage;

public interface ListBlockedUsersUseCase {

    FollowUserPage execute(ListBlockedUsersCommand command);
}
