package com.socialmediablog.platform.services.follower.application.port.in;

import com.socialmediablog.platform.services.follower.application.command.UnfollowUserCommand;
import com.socialmediablog.platform.services.follower.application.result.FollowRelationView;

public interface UnfollowUserUseCase {

    FollowRelationView execute(UnfollowUserCommand command);
}
