package com.socialmediablog.platform.services.follower.application.port.in;

import com.socialmediablog.platform.services.follower.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.follower.application.result.ServiceStatus;

public interface GetServiceStatusUseCase {

    ServiceStatus execute(GetServiceStatusCommand command);
}
