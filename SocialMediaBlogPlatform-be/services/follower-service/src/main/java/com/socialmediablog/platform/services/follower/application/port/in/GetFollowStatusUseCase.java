package com.socialmediablog.platform.services.follower.application.port.in;

import com.socialmediablog.platform.services.follower.application.command.GetFollowStatusCommand;
import com.socialmediablog.platform.services.follower.application.result.FollowStatus;

public interface GetFollowStatusUseCase {

    FollowStatus execute(GetFollowStatusCommand command);
}
