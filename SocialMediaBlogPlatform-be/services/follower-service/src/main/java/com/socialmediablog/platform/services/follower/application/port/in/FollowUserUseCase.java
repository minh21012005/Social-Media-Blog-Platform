package com.socialmediablog.platform.services.follower.application.port.in;

import com.socialmediablog.platform.services.follower.application.command.FollowUserCommand;
import com.socialmediablog.platform.services.follower.application.result.FollowRelationView;

public interface FollowUserUseCase {

    FollowRelationView execute(FollowUserCommand command);
}
