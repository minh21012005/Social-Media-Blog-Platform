package com.socialmediablog.platform.services.follower.application.port.in;

import com.socialmediablog.platform.services.follower.application.command.UnblockUserCommand;
import com.socialmediablog.platform.services.follower.application.result.FollowRelationView;

public interface UnblockUserUseCase {

    FollowRelationView execute(UnblockUserCommand command);
}
