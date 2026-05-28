package com.socialmediablog.platform.services.interaction.application.port.in;

import com.socialmediablog.platform.services.interaction.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.interaction.application.result.ServiceStatus;

public interface GetServiceStatusUseCase {

    ServiceStatus execute(GetServiceStatusCommand command);
}
