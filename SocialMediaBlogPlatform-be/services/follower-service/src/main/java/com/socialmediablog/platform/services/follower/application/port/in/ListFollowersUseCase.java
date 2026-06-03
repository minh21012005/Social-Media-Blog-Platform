package com.socialmediablog.platform.services.follower.application.port.in;

import com.socialmediablog.platform.services.follower.application.command.ListFollowersCommand;
import com.socialmediablog.platform.services.follower.application.result.FollowUserPage;

public interface ListFollowersUseCase {

    FollowUserPage execute(ListFollowersCommand command);
}
