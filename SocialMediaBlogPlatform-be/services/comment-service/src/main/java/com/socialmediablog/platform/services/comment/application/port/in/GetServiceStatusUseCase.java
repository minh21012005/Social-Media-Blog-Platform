package com.socialmediablog.platform.services.comment.application.port.in;

import com.socialmediablog.platform.services.comment.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.comment.application.result.ServiceStatus;

public interface GetServiceStatusUseCase {

    ServiceStatus execute(GetServiceStatusCommand command);
}
