package com.socialmediablog.platform.services.follower.application.port.in;

import com.socialmediablog.platform.services.follower.application.command.ListFollowingCommand;
import com.socialmediablog.platform.services.follower.application.result.FollowUserPage;

public interface ListFollowingUseCase {

    FollowUserPage execute(ListFollowingCommand command);
}
