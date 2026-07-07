package com.socialmediablog.platform.services.follower.application.port.in;

import com.socialmediablog.platform.services.follower.application.command.BlockUserCommand;
import com.socialmediablog.platform.services.follower.application.result.FollowRelationView;

public interface BlockUserUseCase {

    FollowRelationView execute(BlockUserCommand command);
}
