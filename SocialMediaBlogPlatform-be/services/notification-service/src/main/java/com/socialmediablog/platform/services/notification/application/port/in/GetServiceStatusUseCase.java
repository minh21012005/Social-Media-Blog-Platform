package com.socialmediablog.platform.services.notification.application.port.in;

import com.socialmediablog.platform.services.notification.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.notification.application.result.ServiceStatus;

public interface GetServiceStatusUseCase {

    ServiceStatus execute(GetServiceStatusCommand command);
}
