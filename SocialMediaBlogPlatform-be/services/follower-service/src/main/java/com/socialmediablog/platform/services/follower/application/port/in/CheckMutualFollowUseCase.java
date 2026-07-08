package com.socialmediablog.platform.services.follower.application.port.in;

import com.socialmediablog.platform.services.follower.application.command.CheckMutualFollowCommand;
import com.socialmediablog.platform.services.follower.application.result.MutualFollowStatus;

public interface CheckMutualFollowUseCase {

    MutualFollowStatus execute(CheckMutualFollowCommand command);
}
