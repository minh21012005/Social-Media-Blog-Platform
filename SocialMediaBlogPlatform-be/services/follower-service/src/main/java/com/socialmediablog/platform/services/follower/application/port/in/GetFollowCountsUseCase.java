package com.socialmediablog.platform.services.follower.application.port.in;

import com.socialmediablog.platform.services.follower.application.command.GetFollowCountsCommand;
import com.socialmediablog.platform.services.follower.application.result.FollowCounts;

public interface GetFollowCountsUseCase {

    FollowCounts execute(GetFollowCountsCommand command);
}
