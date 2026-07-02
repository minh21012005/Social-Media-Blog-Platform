package com.socialmediablog.platform.services.follower.application.port.in;

import com.socialmediablog.platform.services.follower.application.command.GetBlockStatusCommand;
import com.socialmediablog.platform.services.follower.application.result.BlockStatus;

public interface GetBlockStatusUseCase {

    BlockStatus execute(GetBlockStatusCommand command);
}
