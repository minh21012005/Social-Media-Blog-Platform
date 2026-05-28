package com.socialmediablog.platform.services.article.application.port.in;

import com.socialmediablog.platform.services.article.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.article.application.result.ServiceStatus;

public interface GetServiceStatusUseCase {

    ServiceStatus execute(GetServiceStatusCommand command);
}
